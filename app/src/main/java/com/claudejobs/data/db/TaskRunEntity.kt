package com.claudejobs.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_runs",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("taskId")]
)
data class TaskRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    /** Denormalized snapshot — survives task deletion */
    val taskName: String,
    /** Prompt snapshot at execution time */
    val prompt: String,
    val responseText: String = "",
    /** "SUCCESS" | "FAILED" */
    val status: String,
    val errorMessage: String = "",
    val executedAt: Long = System.currentTimeMillis()
)
