package com.tinhome.momreminder.update

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/frdvch/mom-reminder/releases/latest"
private const val CONNECT_TIMEOUT_MS = 10_000
private const val READ_TIMEOUT_MS = 10_000

sealed interface UpdateResult {
    data class UpdateAvailable(val versionCode: Int, val downloadUrl: String) : UpdateResult
    data object UpToDate : UpdateResult
    data class Error(val message: String) : UpdateResult
}

/** Parses a release tag like "v3" into its versionCode. Returns null for anything else. */
fun parseVersionCodeFromTag(tag: String): Int? = tag.removePrefix("v").toIntOrNull()

object GithubUpdateChecker {

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val json = fetchLatestRelease()
            val tag = json.getString("tag_name")
            val remoteVersionCode = parseVersionCodeFromTag(tag)
                ?: return@withContext UpdateResult.Error("Некоректний тег релізу: $tag")

            if (remoteVersionCode <= currentVersionCode) {
                return@withContext UpdateResult.UpToDate
            }

            val assets = json.getJSONArray("assets")
            if (assets.length() == 0) {
                return@withContext UpdateResult.Error("У релізі немає файлу для завантаження")
            }
            val downloadUrl = assets.getJSONObject(0).getString("browser_download_url")
            UpdateResult.UpdateAvailable(remoteVersionCode, downloadUrl)
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Не вдалося перевірити оновлення")
        }
    }

    private fun fetchLatestRelease(): JSONObject {
        val connection = URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        return try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}
