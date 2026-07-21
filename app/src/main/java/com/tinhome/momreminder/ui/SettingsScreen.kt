package com.tinhome.momreminder.ui

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tinhome.momreminder.BuildConfig
import com.tinhome.momreminder.settings.DigestDaySettings
import com.tinhome.momreminder.update.GithubUpdateChecker
import com.tinhome.momreminder.update.UpdateDownloader
import com.tinhome.momreminder.update.UpdateResult
import java.time.DayOfWeek
import kotlinx.coroutines.launch

private val DAY_LABELS = mapOf(
    DayOfWeek.MONDAY to "Понеділок",
    DayOfWeek.TUESDAY to "Вівторок",
    DayOfWeek.WEDNESDAY to "Середа",
    DayOfWeek.THURSDAY to "Четвер",
    DayOfWeek.FRIDAY to "П'ятниця",
    DayOfWeek.SATURDAY to "Субота",
    DayOfWeek.SUNDAY to "Неділя"
)

@Composable
fun SettingsScreen(viewModel: ReminderViewModel, onDone: () -> Unit) {
    val days by viewModel.digestSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isUpdating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Нагадування на день") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Для кожного дня тижня можна окремо увімкнути список усіх нагадувань на цей день і обрати, о котрій годині його показати.")

            days.forEach { daySettings ->
                DigestDayRow(
                    daySettings = daySettings,
                    onChange = { updated -> viewModel.saveDigestDay(updated.dayOfWeek, updated.enabled, updated.hour, updated.minute) }
                )
            }

            OutlinedButton(
                onClick = {
                    isUpdating = true
                    scope.launch {
                        when (val result = GithubUpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)) {
                            is UpdateResult.UpToDate ->
                                Toast.makeText(context, "У вас остання версія", Toast.LENGTH_SHORT).show()
                            is UpdateResult.UpdateAvailable ->
                                UpdateDownloader.download(context, result.downloadUrl, result.versionCode)
                            is UpdateResult.Error ->
                                Toast.makeText(context, "Не вдалося перевірити оновлення", Toast.LENGTH_SHORT).show()
                        }
                        isUpdating = false
                    }
                },
                enabled = !isUpdating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Оновити зараз")
                }
            }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Готово")
            }
        }
    }
}

@Composable
private fun DigestDayRow(daySettings: DigestDaySettings, onChange: (DigestDaySettings) -> Unit) {
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = DAY_LABELS.getValue(daySettings.dayOfWeek),
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute -> onChange(daySettings.copy(hour = hour, minute = minute)) },
                daySettings.hour, daySettings.minute, true
            ).show()
        }) {
            Text("%02d:%02d".format(daySettings.hour, daySettings.minute))
        }
        Switch(
            checked = daySettings.enabled,
            onCheckedChange = { enabled -> onChange(daySettings.copy(enabled = enabled)) }
        )
    }
}
