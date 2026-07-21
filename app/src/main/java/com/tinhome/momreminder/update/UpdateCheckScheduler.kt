package com.tinhome.momreminder.update

import java.time.LocalDateTime

private const val MORNING_HOUR = 9

/** Finds the next morning check time, strictly after [now]. Independent of digest settings. */
object UpdateCheckScheduler {

    fun nextMorningTrigger(now: LocalDateTime): LocalDateTime {
        val todayMorning = now.toLocalDate().atTime(MORNING_HOUR, 0)
        return if (todayMorning.isAfter(now)) todayMorning else todayMorning.plusDays(1)
    }
}
