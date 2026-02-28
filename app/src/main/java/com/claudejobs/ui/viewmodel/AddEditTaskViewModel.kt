package com.claudejobs.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.claudejobs.data.db.TaskEntity
import com.claudejobs.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    // ── Schedule fields ────────────────────────────────────────────────────
    var name by mutableStateOf("")
    var prompt by mutableStateOf("")
    var scheduleType by mutableStateOf("DAILY")
    var hourOfDay by mutableIntStateOf(9)
    var minuteOfHour by mutableIntStateOf(0)
    var dayOfWeek by mutableIntStateOf(1)
    var intervalHours by mutableIntStateOf(6)
    var runAtEpochMillis by mutableLongStateOf(System.currentTimeMillis() + 3_600_000L)
    var nameError by mutableStateOf(false)
    var promptError by mutableStateOf(false)

    // ── Capability fields ──────────────────────────────────────────────────
    var systemPrompt by mutableStateOf("")
    var maxTokens by mutableIntStateOf(4096)
    var enableWebSearch by mutableStateOf(false)
    var enableWebFetch by mutableStateOf(false)
    var enableCodeExecution by mutableStateOf(false)

    private var editingTaskId: Long = 0L
    private var existingWorkName: String = ""

    fun loadTask(taskId: Long) {
        if (taskId <= 0L) return
        viewModelScope.launch {
            taskRepository.getById(taskId)?.let { task ->
                editingTaskId = task.id
                existingWorkName = task.workName
                name = task.name
                prompt = task.prompt
                scheduleType = task.scheduleType
                hourOfDay = task.hourOfDay
                minuteOfHour = task.minuteOfHour
                dayOfWeek = task.dayOfWeek
                intervalHours = task.intervalHours
                runAtEpochMillis = task.runAtEpochMillis
                // Capabilities
                systemPrompt = task.systemPrompt
                maxTokens = task.maxTokens
                enableWebSearch = task.enableWebSearch
                enableWebFetch = task.enableWebFetch
                enableCodeExecution = task.enableCodeExecution
            }
        }
    }

    fun saveTask(onSuccess: () -> Unit) {
        val validation = TaskFormValidator.validate(name, prompt)
        nameError = validation.nameError
        promptError = validation.promptError
        if (!validation.isValid) return

        viewModelScope.launch {
            val task = TaskEntity(
                id = editingTaskId,
                name = name.trim(),
                prompt = prompt.trim(),
                scheduleType = scheduleType,
                hourOfDay = hourOfDay,
                minuteOfHour = minuteOfHour,
                dayOfWeek = dayOfWeek,
                intervalHours = intervalHours,
                runAtEpochMillis = runAtEpochMillis,
                workName = existingWorkName,
                // Capabilities
                systemPrompt = systemPrompt.trim(),
                maxTokens = maxTokens.coerceIn(256, 128_000),
                enableWebSearch = enableWebSearch,
                enableWebFetch = enableWebFetch,
                enableCodeExecution = enableCodeExecution,
            )
            taskRepository.saveTask(task)
            onSuccess()
        }
    }
}
