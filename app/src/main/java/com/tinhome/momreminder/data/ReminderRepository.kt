package com.tinhome.momreminder.data

import android.content.Context
import com.tinhome.momreminder.alarm.AlarmScheduler
import com.tinhome.momreminder.repeat.NextOccurrenceCalculator
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderRepository(context: Context) {
    private val dao = ReminderDatabase.getInstance(context).reminderDao()
    private val scheduler = AlarmScheduler(context)

    fun observeAll(): Flow<List<Reminder>> = dao.observeAll()

    suspend fun getById(id: Long): Reminder? = dao.getById(id)

    suspend fun observeAllSnapshot(): List<Reminder> = dao.getActive()

    suspend fun save(reminder: Reminder): Long {
        val id = dao.upsert(reminder)
        val saved = reminder.copy(id = if (reminder.id == 0L) id else reminder.id)
        scheduler.schedule(saved)
        return id
    }

    suspend fun delete(reminder: Reminder) {
        scheduler.cancel(reminder)
        dao.delete(reminder)
    }

    /**
     * On boot, plain re-scheduling of a reminder whose time already passed would make
     * AlarmManager fire it immediately - a stale alarm popping up right after the missed
     * notification. Instead: future reminders are just re-scheduled; missed ONCE reminders
     * are deactivated (nothing to catch up to); missed repeating reminders are advanced
     * past every occurrence they slept through, to the next one that's actually still ahead.
     * Returns the reminders that were missed, so the caller can notify about them.
     */
    suspend fun catchUpMissedAndRescheduleActive(): List<Reminder> {
        val now = Instant.now().toEpochMilli()
        val active = dao.getActive()
        val (missed, upcoming) = active.partition { it.dateTimeEpochMillis < now }

        upcoming.forEach { scheduler.schedule(it) }

        missed.forEach { reminder ->
            if (reminder.repeatType == RepeatType.ONCE) {
                deactivate(reminder)
            } else {
                advanceToNextOccurrence(reminder, nextFutureOccurrence(reminder))
            }
        }

        return missed
    }

    private fun nextFutureOccurrence(reminder: Reminder): Long? {
        val now = LocalDateTime.now()
        var current = Instant.ofEpochMilli(reminder.dateTimeEpochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        var guard = 0
        while (guard < 400) {
            val next = NextOccurrenceCalculator.next(current, reminder.repeatType, reminder.repeatData) ?: return null
            if (next.isAfter(now)) return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            current = next
            guard++
        }
        return null
    }

    suspend fun deactivate(reminder: Reminder) {
        dao.update(reminder.copy(isActive = false))
    }

    suspend fun advanceToNextOccurrence(reminder: Reminder, nextEpochMillis: Long?) {
        if (nextEpochMillis == null) {
            deactivate(reminder)
        } else {
            val updated = reminder.copy(dateTimeEpochMillis = nextEpochMillis)
            dao.update(updated)
            scheduler.schedule(updated)
        }
    }
}
