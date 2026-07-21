package com.tinhome.momreminder.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinhome.momreminder.R
import com.tinhome.momreminder.data.RepeatType
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ReminderFormScreen(
    viewModel: ReminderViewModel,
    reminderId: Long?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var repeatType by remember { mutableStateOf(RepeatType.ONCE) }
    var everyNDays by remember { mutableStateOf("1") }
    var selectedWeekDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var dateTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1).withMinute(0)) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(reminderId) {
        if (reminderId != null) {
            viewModel.getReminder(reminderId)?.let { reminder ->
                title = reminder.title
                repeatType = reminder.repeatType
                dateTime = Instant.ofEpochMilli(reminder.dateTimeEpochMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                when (reminder.repeatType) {
                    RepeatType.EVERY_N_DAYS -> everyNDays = reminder.repeatData ?: "1"
                    RepeatType.WEEKLY -> selectedWeekDays = reminder.repeatData
                        ?.split(",")
                        ?.mapNotNull { it.trim().toIntOrNull() }
                        ?.map { DayOfWeek.of(it) }
                        ?.toSet() ?: emptySet()
                    else -> Unit
                }
            }
        } else {
            viewModel.pendingVoiceTitle?.let { title = it }
            viewModel.pendingVoiceTitle = null
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (text != null) {
            title = text
        } else {
            Toast.makeText(context, context.getString(R.string.voice_error), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    stringResource(
                        if (reminderId == null) R.string.add_reminder else R.string.edit_reminder
                    )
                )
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_hint)) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val intent = Intent().apply {
                        action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "uk-UA")
                    }
                    runCatching { voiceLauncher.launch(intent) }
                        .onFailure {
                            Toast.makeText(context, context.getString(R.string.voice_error), Toast.LENGTH_SHORT).show()
                        }
                }) {
                    Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.mic_button_description))
                }
            }

            Text(text = "Повторення")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatChip(RepeatType.ONCE, R.string.repeat_once, repeatType) { repeatType = it }
                RepeatChip(RepeatType.DAILY, R.string.repeat_daily, repeatType) { repeatType = it }
                RepeatChip(RepeatType.WEEKLY, R.string.repeat_weekly, repeatType) { repeatType = it }
                RepeatChip(RepeatType.EVERY_N_DAYS, R.string.repeat_every_n_days, repeatType) { repeatType = it }
            }

            if (repeatType == RepeatType.WEEKLY) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
                    labels.forEachIndexed { index, label ->
                        val day = DayOfWeek.of(index + 1)
                        FilterChip(
                            selected = day in selectedWeekDays,
                            onClick = {
                                selectedWeekDays = if (day in selectedWeekDays) {
                                    selectedWeekDays - day
                                } else {
                                    selectedWeekDays + day
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (repeatType == RepeatType.EVERY_N_DAYS) {
                OutlinedTextField(
                    value = everyNDays,
                    onValueChange = { everyNDays = it.filter { c -> c.isDigit() } },
                    label = { Text("Кожні скільки днів") }
                )
            }

            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            if (repeatType == RepeatType.ONCE) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                dateTime = dateTime.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                            },
                            dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${stringResource(R.string.pick_date)}: ${dateTime.format(dateFormatter)}")
                }
            }
            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            dateTime = dateTime.withHour(hour).withMinute(minute)
                        },
                        dateTime.hour, dateTime.minute, true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${stringResource(R.string.pick_time)}: ${dateTime.format(timeFormatter)}")
            }

            errorText?.let { Text(text = it, color = androidx.compose.ui.graphics.Color.Red) }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorText = "Введи назву нагадування"
                        return@Button
                    }
                    if (repeatType == RepeatType.ONCE && dateTime.isBefore(LocalDateTime.now())) {
                        errorText = context.getString(R.string.past_time_error)
                        return@Button
                    }
                    if (repeatType == RepeatType.WEEKLY && selectedWeekDays.isEmpty()) {
                        errorText = "Обери хоча б один день тижня"
                        return@Button
                    }
                    val repeatData = when (repeatType) {
                        RepeatType.EVERY_N_DAYS -> everyNDays.ifBlank { "1" }
                        RepeatType.WEEKLY -> selectedWeekDays.joinToString(",") { it.value.toString() }
                        else -> null
                    }
                    val firstTrigger = computeFirstTrigger(repeatType, dateTime, selectedWeekDays)
                    val epochMillis = firstTrigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    viewModel.saveReminder(
                        id = reminderId ?: 0L,
                        title = title,
                        dateTimeEpochMillis = epochMillis,
                        repeatType = repeatType,
                        repeatData = repeatData
                    )
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }

            if (reminderId != null) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }

    if (showDeleteConfirm && reminderId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch {
                        viewModel.getReminder(reminderId)?.let { viewModel.deleteReminder(it) }
                        onDone()
                    }
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RepeatChip(value: RepeatType, labelRes: Int, current: RepeatType, onSelect: (RepeatType) -> Unit) {
    FilterChip(
        selected = current == value,
        onClick = { onSelect(value) },
        label = { Text(stringResource(labelRes)) }
    )
}

/**
 * For repeating reminders only the time-of-day the user picked matters, not the date shown
 * in [dateTime] (there's no date picker for them). This finds the first actual occurrence,
 * strictly after now, that matches the chosen time (and, for WEEKLY, the chosen weekdays).
 */
private fun computeFirstTrigger(
    repeatType: RepeatType,
    dateTime: LocalDateTime,
    selectedWeekDays: Set<DayOfWeek>
): LocalDateTime {
    if (repeatType == RepeatType.ONCE) return dateTime

    val now = LocalDateTime.now()
    if (repeatType != RepeatType.WEEKLY) {
        val todayAtTime = now.toLocalDate().atTime(dateTime.hour, dateTime.minute)
        return if (todayAtTime.isAfter(now)) todayAtTime else todayAtTime.plusDays(1)
    }

    var candidateDate = now.toLocalDate()
    repeat(8) {
        val candidate = candidateDate.atTime(dateTime.hour, dateTime.minute)
        if (candidateDate.dayOfWeek in selectedWeekDays && candidate.isAfter(now)) return candidate
        candidateDate = candidateDate.plusDays(1)
    }
    return dateTime
}
