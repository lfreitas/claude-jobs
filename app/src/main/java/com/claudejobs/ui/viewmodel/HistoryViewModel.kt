package com.claudejobs.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.claudejobs.data.db.TaskRunEntity
import com.claudejobs.data.repository.TaskRunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val taskRunRepository: TaskRunRepository
) : ViewModel() {

    val runs = taskRunRepository.getAllRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteRun(run: TaskRunEntity) {
        viewModelScope.launch { taskRunRepository.deleteById(run.id) }
    }
}
