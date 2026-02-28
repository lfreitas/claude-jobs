package com.claudejobs.scheduler

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Tests for [TaskDelayCalculator].
 *
 * All tests supply an explicit [nowMillis] so results are fully deterministic
 * regardless of when the test suite runs.
 */
class TaskDelayCalculatorTest {

    // ── computeDailyDelay ─────────────────────────────────────────────────────

    @Test
    fun `daily delay is positive when target time is in the future`() {
        val now = fixedCal(hour = 10, minute = 0).timeInMillis
        val delay = TaskDelayCalculator.computeDailyDelay(hour = 12, minute = 0, nowMillis = now)
        assertEquals(TimeUnit.HOURS.toMillis(2), delay)
    }

    @Test
    fun `daily delay rolls over to next day when target time has already passed`() {
        // now = 14:00, target = 10:00 → next occurrence is tomorrow at 10:00 = 20 h away
        val now = fixedCal(hour = 14, minute = 0).timeInMillis
        val delay = TaskDelayCalculator.computeDailyDelay(hour = 10, minute = 0, nowMillis = now)
        assertEquals(TimeUnit.HOURS.toMillis(20), delay)
    }

    @Test
    fun `daily delay accounts for minutes correctly`() {
        // now = 09:15, target = 09:45 → 30 min delay
        val now = fixedCal(hour = 9, minute = 15).timeInMillis
        val delay = TaskDelayCalculator.computeDailyDelay(hour = 9, minute = 45, nowMillis = now)
        assertEquals(TimeUnit.MINUTES.toMillis(30), delay)
    }

    @Test
    fun `daily delay rolls over when target equals now exactly`() {
        // Exact same minute as now → should push to next day
        val now = fixedCal(hour = 8, minute = 0).timeInMillis
        val delay = TaskDelayCalculator.computeDailyDelay(hour = 8, minute = 0, nowMillis = now)
        assertEquals(TimeUnit.HOURS.toMillis(24), delay)
    }

    // ── computeWeeklyDelay ────────────────────────────────────────────────────

    @Test
    fun `weekly delay is correct when target day is several days away`() {
        // Pin "now" to a Monday at 10:00, target = Thursday at 10:00 → 3 days
        val now = nextWeekdayAt(Calendar.MONDAY, hour = 10, minute = 0)
        val delay = TaskDelayCalculator.computeWeeklyDelay(
            dayOfWeek = 4,  // ISO 4 = Thursday
            hour = 10, minute = 0,
            nowMillis = now
        )
        assertEquals(TimeUnit.DAYS.toMillis(3), delay)
    }

    @Test
    fun `weekly delay rolls over to next week when target day and time have passed`() {
        // now = Monday 14:00, target = Monday 10:00 → same day but past → 7 days
        val now = nextWeekdayAt(Calendar.MONDAY, hour = 14, minute = 0)
        val delay = TaskDelayCalculator.computeWeeklyDelay(
            dayOfWeek = 1,  // ISO 1 = Monday
            hour = 10, minute = 0,
            nowMillis = now
        )
        assertEquals(TimeUnit.DAYS.toMillis(7), delay)
    }

    @Test
    fun `ISO Sunday (7) maps to Calendar SUNDAY correctly`() {
        // now = Monday 10:00, ISO 7 (Sunday) → 6 days ahead
        val now = nextWeekdayAt(Calendar.MONDAY, hour = 10, minute = 0)
        val delay = TaskDelayCalculator.computeWeeklyDelay(
            dayOfWeek = 7,  // ISO 7 = Sunday
            hour = 10, minute = 0,
            nowMillis = now
        )
        assertEquals(TimeUnit.DAYS.toMillis(6), delay)
    }

    @Test
    fun `ISO Monday (1) maps to Calendar MONDAY correctly`() {
        // now = Sunday 10:00, ISO 1 (Monday) → 1 day ahead
        val now = nextWeekdayAt(Calendar.SUNDAY, hour = 10, minute = 0)
        val delay = TaskDelayCalculator.computeWeeklyDelay(
            dayOfWeek = 1,  // ISO 1 = Monday
            hour = 10, minute = 0,
            nowMillis = now
        )
        assertEquals(TimeUnit.DAYS.toMillis(1), delay)
    }

    @Test
    fun `weekly delay is always positive`() {
        // Exhaustively check all ISO days from any starting Monday
        val base = nextWeekdayAt(Calendar.MONDAY, hour = 0, minute = 0)
        for (iso in 1..7) {
            val delay = TaskDelayCalculator.computeWeeklyDelay(iso, 0, 0, base)
            assertTrue("delay for ISO day $iso should be > 0", delay > 0)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns a Calendar set to today (any date) at the given [hour]:[minute]:00.000. */
    private fun fixedCal(hour: Int, minute: Int): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * Returns epoch millis for the next (or current) occurrence of [calDayOfWeek]
     * at [hour]:[minute].  Used to pin tests to a specific weekday independent of
     * today's actual date.
     */
    private fun nextWeekdayAt(calDayOfWeek: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != calDayOfWeek) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
}
