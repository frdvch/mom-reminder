package com.tinhome.momreminder.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

private const val TAG = "UpdateDownloader"

object UpdateDownloader {

    fun download(context: Context, downloadUrl: String, versionCode: Int) {
        val appContext = context.applicationContext
        val fileName = "mom-reminder-v$versionCode.apk"

        val downloadId = try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Оновлення \"Мама не забудь…\"")
                .setDestinationInExternalFilesDir(appContext, null, fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue update download", e)
            Toast.makeText(appContext, "Не вдалося почати завантаження оновлення", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(appContext, "Завантаження оновлення почалось", Toast.LENGTH_SHORT).show()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completedId != downloadId) return
                appContext.unregisterReceiver(this)
                handleDownloadComplete(appContext, downloadId, fileName)
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun handleDownloadComplete(context: Context, downloadId: Long, fileName: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        val succeeded = cursor.use {
            it.moveToFirst() && it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) ==
                DownloadManager.STATUS_SUCCESSFUL
        }

        if (!succeeded) {
            Log.e(TAG, "Update download did not succeed, id=$downloadId")
            Toast.makeText(
                context,
                "Не вдалося завантажити оновлення. Перевірте інтернет і спробуйте ще раз.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            installApk(context, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch installer", e)
            Toast.makeText(context, "Не вдалося запустити встановлення оновлення", Toast.LENGTH_LONG).show()
        }
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
    }
}
