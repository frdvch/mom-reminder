package com.tinhome.momreminder.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tinhome.momreminder.BuildConfig
import com.tinhome.momreminder.UPDATE_CHANNEL_ID
import com.tinhome.momreminder.ui.MainActivity

private const val UPDATE_NOTIFICATION_ID = 1001

class UpdateCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = GithubUpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)
        if (result is UpdateResult.UpdateAvailable) {
            showUpdateNotification(result.versionCode)
        }
        return Result.success()
    }

    private fun showUpdateNotification(versionCode: Int) {
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Доступне оновлення")
            .setContentText("Версія $versionCode готова до завантаження — Налаштування → Перевірити оновлення")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(UPDATE_NOTIFICATION_ID, notification)
    }
}
