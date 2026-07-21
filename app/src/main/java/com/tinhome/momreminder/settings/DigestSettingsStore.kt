package com.tinhome.momreminder.settings

import android.content.Context
import java.time.DayOfWeek

data class DigestDaySettings(
    val dayOfWeek: DayOfWeek,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

private const val PREFS_NAME = "digest_settings"
private const val DEFAULT_HOUR = 8
private const val DEFAULT_MINUTE = 0

class DigestSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Always returns exactly 7 entries, Monday through Sunday. */
    fun getAll(): List<DigestDaySettings> = DayOfWeek.entries.map { day ->
        DigestDaySettings(
            dayOfWeek = day,
            enabled = prefs.getBoolean(enabledKey(day), false),
            hour = prefs.getInt(hourKey(day), DEFAULT_HOUR),
            minute = prefs.getInt(minuteKey(day), DEFAULT_MINUTE)
        )
    }

    fun saveDay(settings: DigestDaySettings) {
        prefs.edit()
            .putBoolean(enabledKey(settings.dayOfWeek), settings.enabled)
            .putInt(hourKey(settings.dayOfWeek), settings.hour)
            .putInt(minuteKey(settings.dayOfWeek), settings.minute)
            .apply()
    }

    private fun enabledKey(day: DayOfWeek) = "enabled_${day.value}"
    private fun hourKey(day: DayOfWeek) = "hour_${day.value}"
    private fun minuteKey(day: DayOfWeek) = "minute_${day.value}"
}
