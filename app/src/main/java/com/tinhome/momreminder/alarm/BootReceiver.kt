package com.tinhome.momreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tinhome.momreminder.MISSED_CHANNEL_ID
import com.tinhome.momreminder.R
import com.tinhome.momreminder.data.ReminderRepository
import com.tinhome.momreminder.widget.ReminderWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = ReminderRepository(context.applicationContext)
                val missed = repo.catchUpMissedAndRescheduleActive()

                if (missed.isNotEmpty()) {
                    notifyMissed(context, missed.size)
                }

                DigestRescheduler.reschedule(context)
                ReminderWidgetUpdater.requestUpdate(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyMissed(context: Context, count: Int) {
        val notification = NotificationCompat.Builder(context, MISSED_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.missed_notification_title))
            .setContentText("Пропущено нагадувань: $count")
            .setAutoCancel(true)
            .build()
        val manager = androidx.core.app.NotificationManagerCompat.from(context)
        manager.notify(9999, notification)
    }
}
