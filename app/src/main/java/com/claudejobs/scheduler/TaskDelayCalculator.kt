package com.claudejobs.scheduler

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Pure JVM utility that computes WorkManager initial delays for scheduled tasks.
 *
 * Accepting [nowMillis] as a parameter (defaulting to [System.currentTimeMillis])
 * makes every function deterministically testable without mocking the clock.
 */
object TaskDelayCalculator {

    /**
     * Returns the milliseconds until the next occurrence of [hour]:[minute] today,
     * or the same time tomorrow if that moment has already passed.
     */
    fun computeDailyDelay(
        hour: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    /**
     * Returns the milliseconds until the next occurrence of the given ISO [dayOfWeek]
     * (1 = Monday … 7 = Sunday) at [hour]:[minute].
     *
     * If the target day/time is the current moment or in the past, the delay jumps
     * forward by 7 days so the task fires next week.
     */
    fun computeWeeklyDelay(
        dayOfWeek: Int,
        hour: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        // ISO dayOfWeek: 1=Mon…7=Sun → java.util.Calendar: Sun=1, Mon=2…Sat=7
        val calDow = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val currentDow = get(Calendar.DAY_OF_WEEK)
            var daysToAdd = (calDow - currentDow + 7) % 7
            if (daysToAdd == 0 && timeInMillis <= nowMillis) daysToAdd = 7
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
