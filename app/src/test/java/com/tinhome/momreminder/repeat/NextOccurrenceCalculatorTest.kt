package com.tinhome.momreminder.repeat

import com.tinhome.momreminder.data.RepeatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class NextOccurrenceCalculatorTest {

    @Test
    fun `once has no next occurrence`() {
        val now = LocalDateTime.of(2026, 7, 14, 9, 0)
        assertNull(NextOccurrenceCalculator.next(now, RepeatType.ONCE, null))
    }

    @Test
    fun `daily advances exactly one day, same time`() {
        val now = LocalDateTime.of(2026, 7, 14, 9, 0)
        val next = NextOccurrenceCalculator.next(now, RepeatType.DAILY, null)
        assertEquals(LocalDateTime.of(2026, 7, 15, 9, 0), next)
    }

    @Test
    fun `daily rolls over month end correctly`() {
        val now = LocalDateTime.of(2026, 1, 31, 23, 30)
        val next = NextOccurrenceCalculator.next(now, RepeatType.DAILY, null)
        assertEquals(LocalDateTime.of(2026, 2, 1, 23, 30), next)
    }

    @Test
    fun `every n days advances by interval`() {
        val now = LocalDateTime.of(2026, 7, 14, 20, 0)
        val next = NextOccurrenceCalculator.next(now, RepeatType.EVERY_N_DAYS, "3")
        assertEquals(LocalDateTime.of(2026, 7, 17, 20, 0), next)
    }

    @Test
    fun `every n days with invalid data returns null`() {
        val now = LocalDateTime.of(2026, 7, 14, 20, 0)
        assertNull(NextOccurrenceCalculator.next(now, RepeatType.EVERY_N_DAYS, "abc"))
        assertNull(NextOccurrenceCalculator.next(now, RepeatType.EVERY_N_DAYS, "0"))
    }

    @Test
    fun `weekly picks next matching day of week, skipping today`() {
        // 2026-07-14 is a Tuesday (day 2). Reminder repeats Mon(1) and Wed(3).
        val now = LocalDateTime.of(2026, 7, 14, 9, 0)
        val next = NextOccurrenceCalculator.next(now, RepeatType.WEEKLY, "1,3")
        assertEquals(LocalDateTime.of(2026, 7, 15, 9, 0), next) // Wednesday
    }

    @Test
    fun `weekly wraps around into next week`() {
        // 2026-07-17 is a Friday (day 5). Only Monday(1) is selected -> next Monday.
        val now = LocalDateTime.of(2026, 7, 17, 9, 0)
        val next = NextOccurrenceCalculator.next(now, RepeatType.WEEKLY, "1")
        assertEquals(LocalDateTime.of(2026, 7, 20, 9, 0), next)
    }

    @Test
    fun `weekly with empty or malformed data returns null`() {
        val now = LocalDateTime.of(2026, 7, 14, 9, 0)
        assertNull(NextOccurrenceCalculator.next(now, RepeatType.WEEKLY, null))
        assertNull(NextOccurrenceCalculator.next(now, RepeatType.WEEKLY, ""))
    }
}
