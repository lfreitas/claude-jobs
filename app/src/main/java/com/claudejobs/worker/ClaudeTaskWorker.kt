package com.claudejobs.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.claudejobs.data.api.ClaudeApiClient
import com.claudejobs.data.api.dto.ApiMessage
import com.claudejobs.data.api.dto.MessageRequest
import com.claudejobs.data.db.TaskDao
import com.claudejobs.data.db.TaskRunEntity
import com.claudejobs.data.prefs.ApiKeyStore
import com.claudejobs.data.repository.TaskRunRepository
import com.claudejobs.notification.NotificationHelper
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
            val service = ClaudeApiClient.create(apiKey)
            val response = service.sendMessage(
                MessageRequest(
                    model = model,
                    messages = listOf(ApiMessage(content = task.prompt))
                )
            )
            val text = response.content.firstOrNull { it.type == "text" }?.text ?: ""
            val run = TaskRunEntity(
                taskId = task.id,
                taskName = task.name,
                prompt = task.prompt,
                responseText = text,
                status = "SUCCESS",
                executedAt = startedAt
            )
            val runId = taskRunRepository.insert(run)
            notificationHelper.postSuccess(task.name, text.take(150), runId)
            Result.success()
        } catch (e: Exception) {
            val run = TaskRunEntity(
                taskId = task.id,
                taskName = task.name,
                prompt = task.prompt,
                status = "FAILED",
                errorMessage = e.message ?: "Unknown error",
                executedAt = startedAt
            )
            taskRunRepository.insert(run)
            notificationHelper.postError(task.name, e.message ?: "Unknown error")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
