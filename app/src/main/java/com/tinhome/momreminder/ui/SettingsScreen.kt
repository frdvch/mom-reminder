package com.tinhome.momreminder.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tinhome.momreminder.settings.DigestDaySettings
import java.time.DayOfWeek

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
