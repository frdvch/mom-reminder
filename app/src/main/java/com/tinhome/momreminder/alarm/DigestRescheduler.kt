package com.tinhome.momreminder.alarm

import android.content.Context
import com.tinhome.momreminder.settings.DigestSettingsStore
import com.tinhome.momreminder.settings.DigestTimeCalculator
import java.time.LocalDateTime
import java.time.ZoneId

/** Recomputes and (re)schedules the next per-weekday digest firing, or cancels it if none is enabled. */
object DigestRescheduler {
    fun reschedule(context: Context) {
        val days = DigestSettingsStore(context).getAll()
        val next = DigestTimeCalculator.nextTrigger(LocalDateTime.now(), days)
        val scheduler = AlarmScheduler(context)
        if (next == null) {
            scheduler.cancelDigest()
        } else {
            scheduler.scheduleDigestAt(next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }
    }
}
