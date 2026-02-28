package com.claudejobs.data.repository

import android.content.Context
import androidx.work.*
import com.claudejobs.data.db.TaskDao
import com.claudejobs.data.db.TaskEntity
import com.claudejobs.scheduler.TaskScheduler
import com.claudejobs.worker.ClaudeTaskWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
    private val taskScheduler: TaskScheduler
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getById(id: Long): TaskEntity? = taskDao.getById(id)

    /** Insert or update a task. Sets workName on new tasks, reschedules on edits. */
    suspend fun saveTask(task: TaskEntity): Long {
        return if (task.id == 0L) {
            // Insert first to get the auto-generated ID
            val newId = taskDao.insert(task.copy(workName = ""))
            val workName = "task_$newId"
            val updated = task.copy(id = newId, workName = workName)
            taskDao.update(updated)
            if (updated.isEnabled) taskScheduler.schedule(updated)
            newId
        } else {
            // Cancel old schedule then update + reschedule
            if (task.workName.isNotEmpty()) taskScheduler.cancel(task.workName)
            taskDao.update(task)
            if (task.isEnabled) taskScheduler.schedule(task)
            task.id
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        if (task.workName.isNotEmpty()) taskScheduler.cancel(task.workName)
        taskDao.delete(task)
    }

    suspend fun setEnabled(task: TaskEntity, enabled: Boolean) {
        val updated = task.copy(isEnabled = enabled)
        taskDao.update(updated)
        if (enabled) {
            taskScheduler.schedule(updated)
        } else {
            if (task.workName.isNotEmpty()) taskScheduler.cancel(task.workName)
        }
    }

    /** Enqueue an immediate one-shot run for testing. */
    suspend fun runNow(taskId: Long) {
        taskDao.getById(taskId) ?: return
        val request = OneTimeWorkRequestBuilder<ClaudeTaskWorker>()
            .setInputData(workDataOf(ClaudeTaskWorker.KEY_TASK_ID to taskId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
