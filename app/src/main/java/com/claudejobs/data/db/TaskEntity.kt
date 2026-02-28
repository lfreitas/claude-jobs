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
    val createdAt: Long = System.currentTimeMillis(),

    // ── Claude Capabilities ──────────────────────────────────────────────────

    /** Optional system prompt prepended to the Claude request. */
    val systemPrompt: String = "",
    /**
     * Maximum tokens Claude may generate per run.
     * 4096 is a safe default that allows tool-use loops without being wasteful.
     */
    val maxTokens: Int = 4096,
    /** When true, supplies the web_search_20260209 server-side tool. */
    val enableWebSearch: Boolean = false,
    /** When true, supplies the web_fetch_20260209 server-side tool. */
    val enableWebFetch: Boolean = false,
    /** When true, supplies the code_execution_20260120 server-side tool. */
    val enableCodeExecution: Boolean = false,
)
