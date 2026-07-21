package com.tinhome.momreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tinhome.momreminder.alarm.DigestRescheduler
import com.tinhome.momreminder.data.Reminder
import com.tinhome.momreminder.data.ReminderRepository
import com.tinhome.momreminder.data.RepeatType
import com.tinhome.momreminder.settings.DigestDaySettings
import com.tinhome.momreminder.settings.DigestSettingsStore
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReminderRepository(application)
    private val digestStore = DigestSettingsStore(application)

    val reminders: StateFlow<List<Reminder>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _digestSettings = MutableStateFlow(digestStore.getAll())
    val digestSettings: StateFlow<List<DigestDaySettings>> = _digestSettings

    suspend fun getReminder(id: Long): Reminder? = repository.getById(id)

    /** One-shot hand-off: voice text captured on the home screen, consumed by the form screen. */
    var pendingVoiceTitle by mutableStateOf<String?>(null)

    fun saveReminder(
        id: Long,
        title: String,
        dateTimeEpochMillis: Long,
        repeatType: RepeatType,
        repeatData: String?
    ) {
        viewModelScope.launch {
            repository.save(
                Reminder(
                    id = id,
                    title = title,
                    dateTimeEpochMillis = dateTimeEpochMillis,
                    repeatType = repeatType,
                    repeatData = repeatData,
                    isActive = true
                )
            )
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { repository.delete(reminder) }
    }

    fun saveDigestDay(dayOfWeek: DayOfWeek, enabled: Boolean, hour: Int, minute: Int) {
        val daySettings = DigestDaySettings(dayOfWeek, enabled, hour, minute)
        digestStore.saveDay(daySettings)
        _digestSettings.value = digestStore.getAll()
        DigestRescheduler.reschedule(getApplication())
    }
}
