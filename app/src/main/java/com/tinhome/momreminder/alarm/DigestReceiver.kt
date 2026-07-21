package com.tinhome.momreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tinhome.momreminder.DIGEST_CHANNEL_ID
import com.tinhome.momreminder.data.Reminder
import com.tinhome.momreminder.data.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DIGEST_NOTIFICATION_ID = 8888

class DigestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY_DIGEST) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = ReminderRepository(context.applicationContext)
                val today = LocalDate.now(ZoneId.systemDefault())
                val todaysReminders = repo.observeAllSnapshot()
                    .filter { isOnDate(it, today) }
                    .sortedBy { it.dateTimeEpochMillis }

                showDigestNotification(context, todaysReminders)
                DigestRescheduler.reschedule(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isOnDate(reminder: Reminder, date: LocalDate): Boolean {
        val reminderDate = Instant.ofEpochMilli(reminder.dateTimeEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return reminderDate == date
    }

    private fun showDigestNotification(context: Context, reminders: List<Reminder>) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val title = if (reminders.isEmpty()) {
            "На сьогодні нагадувань немає"
        } else {
            "Нагадування на сьогодні (${reminders.size})"
        }

        val builder = NotificationCompat.Builder(context, DIGEST_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setAutoCancel(true)

        if (reminders.isNotEmpty()) {
            val inboxStyle = NotificationCompat.InboxStyle()
            reminders.forEach { reminder ->
                val time = Instant.ofEpochMilli(reminder.dateTimeEpochMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter)
                inboxStyle.addLine("$time • ${reminder.title}")
            }
            builder.setStyle(inboxStyle)
            builder.setContentText(reminders.first().title)
        }

        NotificationManagerCompat.from(context).notify(DIGEST_NOTIFICATION_ID, builder.build())
    }
}
