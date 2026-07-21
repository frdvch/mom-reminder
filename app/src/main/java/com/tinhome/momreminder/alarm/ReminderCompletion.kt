package com.tinhome.momreminder.alarm

import com.tinhome.momreminder.data.Reminder
import com.tinhome.momreminder.data.ReminderRepository
import com.tinhome.momreminder.repeat.NextOccurrenceCalculator
import java.time.Instant
import java.time.ZoneId

private const val SNOOZE_MINUTES = 10L

/** Shared completion logic used by both the full-screen alarm and gentle-notification actions. */
object ReminderCompletion {

    suspend fun markDone(repo: ReminderRepository, reminder: Reminder) {
        val currentDateTime = Instant.ofEpochMilli(reminder.dateTimeEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val next = NextOccurrenceCalculator.next(currentDateTime, reminder.repeatType, reminder.repeatData)
        val nextEpoch = next?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        repo.advanceToNextOccurrence(reminder, nextEpoch)
    }

    suspend fun snooze(repo: ReminderRepository, reminder: Reminder) {
        val snoozeTime = Instant.now().plusSeconds(SNOOZE_MINUTES * 60).toEpochMilli()
        repo.save(reminder.copy(dateTimeEpochMillis = snoozeTime))
    }
}
