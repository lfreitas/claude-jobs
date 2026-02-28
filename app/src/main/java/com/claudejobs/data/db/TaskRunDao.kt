package com.claudejobs.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskRunDao {
    @Query("SELECT * FROM task_runs ORDER BY executedAt DESC")
    fun getAllRuns(): Flow<List<TaskRunEntity>>

    @Query("SELECT * FROM task_runs WHERE taskId = :taskId ORDER BY executedAt DESC")
    fun getRunsForTask(taskId: Long): Flow<List<TaskRunEntity>>

    @Query("SELECT * FROM task_runs WHERE id = :id")
    suspend fun getById(id: Long): TaskRunEntity?

    @Insert
    suspend fun insert(run: TaskRunEntity): Long

    @Query("DELETE FROM task_runs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM task_runs WHERE taskId = :taskId ORDER BY executedAt DESC LIMIT 1")
    suspend fun getLatestForTask(taskId: Long): TaskRunEntity?
}
