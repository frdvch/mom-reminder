package com.tinhome.momreminder.repeat

import com.tinhome.momreminder.data.RepeatType
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Computes the next trigger time strictly after [current], given the reminder's
 * repeat configuration. Returns null when the reminder has no further occurrences
 * (ONCE, or malformed repeatData).
 */
object NextOccurrenceCalculator {

    fun next(current: LocalDateTime, repeatType: RepeatType, repeatData: String?): LocalDateTime? {
        return when (repeatType) {
            RepeatType.ONCE -> null
            RepeatType.DAILY -> current.plusDays(1)
            RepeatType.EVERY_N_DAYS -> {
                val n = repeatData?.toIntOrNull() ?: return null
                if (n <= 0) null else current.plusDays(n.toLong())
            }
            RepeatType.WEEKLY -> nextWeekly(current, repeatData)
        }
    }

    private fun nextWeekly(current: LocalDateTime, repeatData: String?): LocalDateTime? {
        val days = repeatData
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in 1..7 }
            ?.map { DayOfWeek.of(it) }
            ?.toSet()
            ?: return null
        if (days.isEmpty()) return null

        var candidate = current.plusDays(1)
        repeat(8) {
            if (candidate.dayOfWeek in days) return candidate
            candidate = candidate.plusDays(1)
        }
        return null
    }
}
