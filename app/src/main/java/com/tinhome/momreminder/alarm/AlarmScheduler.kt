package com.tinhome.momreminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tinhome.momreminder.data.Reminder
import com.tinhome.momreminder.ui.MainActivity

const val EXTRA_REMINDER_ID = "extra_reminder_id"
const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
const val ACTION_DAILY_DIGEST = "com.tinhome.momreminder.ACTION_DAILY_DIGEST"

private const val DIGEST_REQUEST_CODE = -1

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        if (!reminder.isActive) {
            cancel(reminder)
            return
        }
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_TITLE, reminder.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // setAlarmClock (not setExactAndAllowWhileIdle) is what the stock Clock app uses -
        // the OS is not allowed to shift it for battery savings, unlike other "exact" alarms.
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(reminder.dateTimeEpochMillis, showIntent),
            pendingIntent
        )
    }

    fun cancel(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /** Schedules the next digest firing at the given absolute moment. */
    fun scheduleDigestAt(epochMillis: Long) {
        val intent = Intent(context, DigestReceiver::class.java).apply {
            action = ACTION_DAILY_DIGEST
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DIGEST_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            epochMillis,
            pendingIntent
        )
    }

    fun cancelDigest() {
        val intent = Intent(context, DigestReceiver::class.java).apply {
            action = ACTION_DAILY_DIGEST
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DIGEST_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
