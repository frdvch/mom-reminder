package com.tinhome.momreminder.update

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCheckSchedulerTest {

    @Test
    fun `before 9am schedules later today`() {
        val now = LocalDateTime.of(2026, 7, 22, 6, 30)
        assertEquals(LocalDateTime.of(2026, 7, 22, 9, 0), UpdateCheckScheduler.nextMorningTrigger(now))
    }

    @Test
    fun `after 9am schedules tomorrow`() {
        val now = LocalDateTime.of(2026, 7, 22, 14, 0)
        assertEquals(LocalDateTime.of(2026, 7, 23, 9, 0), UpdateCheckScheduler.nextMorningTrigger(now))
    }

    @Test
    fun `exactly 9am schedules tomorrow`() {
        val now = LocalDateTime.of(2026, 7, 22, 9, 0)
        assertEquals(LocalDateTime.of(2026, 7, 23, 9, 0), UpdateCheckScheduler.nextMorningTrigger(now))
    }
}
