package com.tinhome.momreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val ALARM_CHANNEL_ID = "alarm_channel"
const val DIGEST_CHANNEL_ID = "digest_channel"
const val MISSED_CHANNEL_ID = "missed_channel"

class MomReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)

        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Нагадування-алярми",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Повноекранні нагадування зі звуком"
            enableVibration(true)
        }

        val digestChannel = NotificationChannel(
            DIGEST_CHANNEL_ID,
            "Нагадування на день",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Щоденний список усіх нагадувань"
        }

        val missedChannel = NotificationChannel(
            MISSED_CHANNEL_ID,
            "Пропущені нагадування",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannels(listOf(alarmChannel, digestChannel, missedChannel))
    }
}
