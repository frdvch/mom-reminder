package com.tinhome.momreminder.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class DigestTimeCalculatorTest {

    private fun day(dayOfWeek: DayOfWeek, enabled: Boolean, hour: Int = 8, minute: Int = 0) =
        DigestDaySettings(dayOfWeek, enabled, hour, minute)

    @Test
    fun `no days enabled returns null`() {
        val now = LocalDateTime.of(2026, 7, 14, 9, 0) // Tuesday
        val days = DayOfWeek.entries.map { day(it, enabled = false) }
        assertNull(DigestTimeCalculator.nextTrigger(now, days))
    }

    @Test
    fun `picks today's time when still in the future`() {
        val now = LocalDateTime.of(2026, 7, 14, 7, 0) // Tuesday 07:00
        val days = DayOfWeek.entries.map {
            day(it, enabled = it == DayOfWeek.TUESDAY, hour = 9, minute = 30)
        }
        val next = DigestTimeCalculator.nextTrigger(now, days)
        assertEquals(LocalDateTime.of(2026, 7, 14, 9, 30), next)
    }

    @Test
    fun `skips today when today's time already passed`() {
        val now = LocalDateTime.of(2026, 7, 14, 20, 0) // Tuesday 20:00, past 09:30
        val days = DayOfWeek.entries.map {
            day(it, enabled = it == DayOfWeek.TUESDAY, hour = 9, minute = 30)
        }
        val next = DigestTimeCalculator.nextTrigger(now, days)
        assertEquals(LocalDateTime.of(2026, 7, 21, 9, 30), next)
    }

    @Test
    fun `different times per day picks the nearest one`() {
        val now = LocalDateTime.of(2026, 7, 14, 9, 0) // Tuesday 09:00
        val days = listOf(
            day(DayOfWeek.TUESDAY, enabled = true, hour = 20, minute = 0),
            day(DayOfWeek.WEDNESDAY, enabled = true, hour = 7, minute = 0),
            day(DayOfWeek.MONDAY, enabled = false),
            day(DayOfWeek.THURSDAY, enabled = false),
            day(DayOfWeek.FRIDAY, enabled = false),
            day(DayOfWeek.SATURDAY, enabled = false),
            day(DayOfWeek.SUNDAY, enabled = false)
        )
        val next = DigestTimeCalculator.nextTrigger(now, days)
        assertEquals(LocalDateTime.of(2026, 7, 14, 20, 0), next)
    }
}
