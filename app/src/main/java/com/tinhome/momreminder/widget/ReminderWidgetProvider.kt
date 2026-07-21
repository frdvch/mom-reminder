package com.tinhome.momreminder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.tinhome.momreminder.R
import com.tinhome.momreminder.alarm.EXTRA_REMINDER_ID
import com.tinhome.momreminder.alarm.ReminderCompletion
import com.tinhome.momreminder.data.Reminder
import com.tinhome.momreminder.data.ReminderRepository
import com.tinhome.momreminder.ui.MainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val ACTION_MARK_DONE = "com.tinhome.momreminder.widget.ACTION_MARK_DONE"

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

class ReminderWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MARK_DONE) {
            handleMarkDone(context, intent)
            return
        }
        super.onReceive(context, intent)
    }

    private fun handleMarkDone(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = ReminderRepository(context.applicationContext)
                val reminder = repo.getById(reminderId)
                if (reminder != null) {
                    ReminderCompletion.markDone(repo, reminder)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = ReminderRepository(context.applicationContext)
                val nextReminder = repo.getNextActiveReminder()
                val views = buildRemoteViews(context, nextReminder)
                appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildRemoteViews(context: Context, reminder: Reminder?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.reminder_widget)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        if (reminder == null) {
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_empty_state))
            views.setViewVisibility(R.id.widget_time, View.GONE)
            views.setViewVisibility(R.id.widget_done_button, View.GONE)
            return views
        }

        val time = Instant.ofEpochMilli(reminder.dateTimeEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(TIME_FORMATTER)

        views.setTextViewText(R.id.widget_title, reminder.title)
        views.setViewVisibility(R.id.widget_time, View.VISIBLE)
        views.setTextViewText(R.id.widget_time, time)
        views.setViewVisibility(R.id.widget_done_button, View.VISIBLE)

        val doneIntent = Intent(context, ReminderWidgetProvider::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, reminder.id.toInt(), doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_done_button, donePendingIntent)

        return views
    }
}
