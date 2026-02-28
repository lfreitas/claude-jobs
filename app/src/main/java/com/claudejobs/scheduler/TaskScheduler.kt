package com.claudejobs.scheduler

import android.content.Context
import androidx.work.*
import com.claudejobs.data.db.TaskEntity
import com.claudejobs.worker.ClaudeTaskWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    fun schedule(task: TaskEntity) {
        if (!task.isEnabled || task.workName.isEmpty()) return
        when (task.scheduleType) {
            "ONE_TIME"  -> scheduleOneTime(task)
            "INTERVAL"  -> scheduleInterval(task)
            "DAILY"     -> scheduleDaily(task)
            "WEEKLY"    -> scheduleWeekly(task)
        }
    }

    fun cancel(workName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    private fun scheduleOneTime(task: TaskEntity) {
        val delay = (task.runAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ClaudeTaskWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ClaudeTaskWorker.KEY_TASK_ID to task.id))
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(task.workName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun scheduleInterval(task: TaskEntity) {
        val hours = task.intervalHours.toLong().coerceAtLeast(1L)
        val request = PeriodicWorkRequestBuilder<ClaudeTaskWorker>(hours, TimeUnit.HOURS)
            .setInputData(workDataOf(ClaudeTaskWorker.KEY_TASK_ID to task.id))
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(task.workName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun scheduleDaily(task: TaskEntity) {
        val delay = computeDailyDelay(task.hourOfDay, task.minuteOfHour)
        val request = PeriodicWorkRequestBuilder<ClaudeTaskWorker>(24L, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ClaudeTaskWorker.KEY_TASK_ID to task.id))
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(task.workName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun scheduleWeekly(task: TaskEntity) {
        val delay = computeWeeklyDelay(task.dayOfWeek, task.hourOfDay, task.minuteOfHour)
        val request = PeriodicWorkRequestBuilder<ClaudeTaskWorker>(7L, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ClaudeTaskWorker.KEY_TASK_ID to task.id))
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(task.workName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun computeDailyDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun computeWeeklyDelay(dayOfWeek: Int, hour: Int, minute: Int): Long {
        // ISO dayOfWeek: 1=Mon…7=Sun → Calendar: Sun=1, Mon=2…Sat=7
        val calDow = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val currentDow = get(Calendar.DAY_OF_WEEK)
            var daysToAdd = (calDow - currentDow + 7) % 7
            if (daysToAdd == 0 && timeInMillis <= now.timeInMillis) daysToAdd = 7
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
