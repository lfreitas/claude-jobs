package com.claudejobs.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val prompt: String,
    /** "DAILY" | "WEEKLY" | "INTERVAL" | "ONE_TIME" */
    val scheduleType: String,
    val hourOfDay: Int = 9,
    val minuteOfHour: Int = 0,
    /** ISO day: 1=Mon … 7=Sun (WEEKLY only) */
    val dayOfWeek: Int = 1,
    /** Repeat interval in hours (INTERVAL only) */
    val intervalHours: Int = 6,
    /** Epoch millis for exact run time (ONE_TIME only) */
    val runAtEpochMillis: Long = 0L,
    val isEnabled: Boolean = true,
    /** WorkManager unique work name, e.g. "task_42" */
    val workName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
