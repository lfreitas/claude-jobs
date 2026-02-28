package com.claudejobs.data.repository

import com.claudejobs.data.db.TaskRunDao
import com.claudejobs.data.db.TaskRunEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRunRepository @Inject constructor(private val taskRunDao: TaskRunDao) {

    fun getAllRuns(): Flow<List<TaskRunEntity>> = taskRunDao.getAllRuns()

    fun getRunsForTask(taskId: Long): Flow<List<TaskRunEntity>> =
        taskRunDao.getRunsForTask(taskId)

    suspend fun getById(id: Long): TaskRunEntity? = taskRunDao.getById(id)

    suspend fun insert(run: TaskRunEntity): Long = taskRunDao.insert(run)

    suspend fun deleteById(id: Long) = taskRunDao.deleteById(id)
}
