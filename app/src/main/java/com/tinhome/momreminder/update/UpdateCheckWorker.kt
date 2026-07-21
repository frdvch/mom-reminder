package com.tinhome.momreminder.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tinhome.momreminder.BuildConfig

/**
 * Runs every morning (see [com.tinhome.momreminder.MomReminderApp]). If a newer version is
 * found, downloads it immediately — [UpdateDownloader] itself shows the "ready to install"
 * system notification once the download completes, so no separate "update available"
 * notification is needed here.
 */
class UpdateCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = GithubUpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)
        if (result is UpdateResult.UpdateAvailable) {
            UpdateDownloader.download(applicationContext, result.downloadUrl, result.versionCode)
        }
        return Result.success()
    }
}
