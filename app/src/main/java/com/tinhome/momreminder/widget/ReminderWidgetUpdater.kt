package com.tinhome.momreminder.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Forces [ReminderWidgetProvider] to redraw right after reminder data changes. */
object ReminderWidgetUpdater {

    fun requestUpdate(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val componentName = ComponentName(appContext, ReminderWidgetProvider::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = componentName
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        appContext.sendBroadcast(intent)
    }
}
