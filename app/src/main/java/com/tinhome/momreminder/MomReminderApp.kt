package com.tinhome.momreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tinhome.momreminder.update.UpdateCheckScheduler
import com.tinhome.momreminder.update.UpdateCheckWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

const val ALARM_CHANNEL_ID = "alarm_channel"
const val DIGEST_CHANNEL_ID = "digest_channel"
const val MISSED_CHANNEL_ID = "missed_channel"
const val UPDATE_CHANNEL_ID = "update_channel"

private const val OLD_UPDATE_CHECK_WORK_NAME = "update_check"
private const val UPDATE_CHECK_WORK_NAME = "update_check_morning"
private const val UPDATE_CHECK_INTERVAL_HOURS = 24L

class MomReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleUpdateCheck()
    }

    private fun scheduleUpdateCheck() {
        val workManager = WorkManager.getInstance(this)
        // Migration from the old always-KEEP schedule (arbitrary time-of-day) to a fixed morning check.
        workManager.cancelUniqueWork(OLD_UPDATE_CHECK_WORK_NAME)

        val initialDelay = Duration.between(
            LocalDateTime.now(),
            UpdateCheckScheduler.nextMorningTrigger(LocalDateTime.now())
        )
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            UPDATE_CHECK_INTERVAL_HOURS, TimeUnit.HOURS
        ).setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()

        workManager.enqueueUniquePeriodicWork(
            UPDATE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
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

        val updateChannel = NotificationChannel(
            UPDATE_CHANNEL_ID,
            "Оновлення застосунку",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Повідомляє, коли доступна нова версія застосунку"
        }

        manager.createNotificationChannels(listOf(alarmChannel, digestChannel, missedChannel, updateChannel))
    }
}
