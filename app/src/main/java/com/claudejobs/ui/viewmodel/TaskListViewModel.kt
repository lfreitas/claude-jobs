package com.claudejobs.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.claudejobs.data.db.TaskEntity
import com.claudejobs.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }

    fun setEnabled(task: TaskEntity, enabled: Boolean) {
        viewModelScope.launch { taskRepository.setEnabled(task, enabled) }
    }

    fun runNow(taskId: Long) {
        viewModelScope.launch { taskRepository.runNow(taskId) }
    }
}
