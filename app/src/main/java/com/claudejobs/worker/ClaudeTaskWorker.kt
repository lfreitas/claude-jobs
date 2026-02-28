package com.claudejobs.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.claudejobs.data.api.ClaudeApiClient
import com.claudejobs.data.api.dto.ApiMessage
import com.claudejobs.data.api.dto.MessageRequest
import com.claudejobs.data.api.dto.ToolDefinition
import com.claudejobs.data.db.TaskDao
import com.claudejobs.data.db.TaskRunEntity
import com.claudejobs.data.prefs.ApiKeyStore
import com.claudejobs.data.repository.TaskRunRepository
import com.claudejobs.notification.NotificationHelper
import com.claudejobs.data.api.dto.AnthropicErrorResponse
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ClaudeTaskWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskDao: TaskDao,
    private val taskRunRepository: TaskRunRepository,
    private val apiKeyStore: ApiKeyStore,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_TASK_ID = "task_id"

        /** Maximum continuation rounds for pause_turn loops. */
        private const val MAX_CONTINUATION_ROUNDS = 5
    }

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId == -1L) return Result.failure()

        val task = taskDao.getById(taskId) ?: return Result.failure()
        val apiKey = apiKeyStore.getApiKey()
        val model = apiKeyStore.getModel()

        if (apiKey.isBlank()) {
            notificationHelper.postError(task.name, "API key not configured. Open Settings to add it.")
            return Result.failure()
        }

        val startedAt = System.currentTimeMillis()

        return try {
            // ── Build tool list ────────────────────────────────────────────────
            val tools: List<ToolDefinition>? = buildList {
                if (task.enableWebSearch)     add(ToolDefinition("web_search_20260209",    "web_search"))
                if (task.enableWebFetch)      add(ToolDefinition("web_fetch_20260209",     "web_fetch"))
                if (task.enableCodeExecution) add(ToolDefinition("code_execution_20260120","code_execution"))
            }.ifEmpty { null }

            // Dynamic web-search filtering requires code execution + a beta header.
            val betaHeader: String? = if (
                task.enableCodeExecution && (task.enableWebSearch || task.enableWebFetch)
            ) {
                "code-execution-web-tools-2026-02-09"
            } else {
                null
            }

            val service = ClaudeApiClient.create(apiKey, betaHeader)

            // ── Agentic loop ───────────────────────────────────────────────────
            // Server-side tools (web search, code execution) are executed entirely
            // by Anthropic's servers. The app only needs to handle pause_turn, which
            // occurs when the server's tool loop reaches its 10-iteration limit.
            val messages = mutableListOf<ApiMessage>(
                ApiMessage(role = "user", content = task.prompt)
            )

            val textAccumulator = StringBuilder()
            var finalStopReason = "end_turn"

            for (round in 0 until MAX_CONTINUATION_ROUNDS) {
                val request = MessageRequest(
                    model = model,
                    max_tokens = task.maxTokens,
                    system = task.systemPrompt.takeIf { it.isNotBlank() },
                    tools = tools,
                    messages = messages
                )

                val httpResponse = service.sendMessage(request)

                if (!httpResponse.isSuccessful) {
                    val rawBody = runCatching { httpResponse.errorBody()?.string() }.getOrNull()
                    val parsed = runCatching {
                        Gson().fromJson(rawBody, AnthropicErrorResponse::class.java)
                    }.getOrNull()
                    val detail = parsed?.error?.message?.takeIf { it.isNotBlank() } ?: rawBody
                    error("HTTP ${httpResponse.code()}: ${detail ?: httpResponse.message()}")
                }

                val body = httpResponse.body()
                    ?: error("Empty response body from API")

                // Accumulate text from this response round.
                body.content.forEach { block ->
                    if (block.type == "text") {
                        textAccumulator.append(block.text ?: "")
                    }
                }

                finalStopReason = body.stop_reason

                when (body.stop_reason) {
                    "end_turn", "max_tokens", "stop_sequence" -> break

                    "pause_turn" -> {
                        // Server tool loop hit its limit — append the assistant turn and
                        // send a continuation user message to resume on the server.
                        messages.add(ApiMessage(role = "assistant", content = body.content))
                        messages.add(ApiMessage(role = "user", content = "Continue."))
                        // Loop again
                    }

                    else -> break // Unknown stop reason — stop safely
                }
            }

            val resultText = textAccumulator.toString().trim()
                .ifBlank { "(No text response — stop_reason: $finalStopReason)" }

            val run = TaskRunEntity(
                taskId = task.id,
                taskName = task.name,
                prompt = task.prompt,
                responseText = resultText,
                status = "SUCCESS",
                executedAt = startedAt
            )
            val runId = taskRunRepository.insert(run)
            notificationHelper.postSuccess(task.name, resultText.take(150), runId)
            Result.success()

        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            val run = TaskRunEntity(
                taskId = task.id,
                taskName = task.name,
                prompt = task.prompt,
                status = "FAILED",
                errorMessage = errorMsg,
                executedAt = startedAt
            )
            taskRunRepository.insert(run)
            notificationHelper.postError(task.name, errorMsg)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
