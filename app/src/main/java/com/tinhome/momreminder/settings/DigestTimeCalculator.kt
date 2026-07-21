package com.tinhome.momreminder.settings

import java.time.LocalDateTime

/** Finds the next moment a per-day digest should fire, strictly after [now]. */
object DigestTimeCalculator {

    fun nextTrigger(now: LocalDateTime, days: List<DigestDaySettings>): LocalDateTime? {
        val enabledByDay = days.filter { it.enabled }.associateBy { it.dayOfWeek }
        if (enabledByDay.isEmpty()) return null

        val today = now.toLocalDate()
        repeat(8) { offset ->
            val candidateDate = today.plusDays(offset.toLong())
            val daySettings = enabledByDay[candidateDate.dayOfWeek] ?: return@repeat
            val candidate = candidateDate.atTime(daySettings.hour, daySettings.minute)
            if (candidate.isAfter(now)) return candidate
        }
        return null
    }
}
