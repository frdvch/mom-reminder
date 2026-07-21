package com.tinhome.momreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RepeatType { ONCE, DAILY, WEEKLY, EVERY_N_DAYS }

/**
 * repeatData meaning depends on repeatType:
 *  - ONCE / DAILY: unused (null)
 *  - WEEKLY: comma-separated ISO day-of-week numbers, e.g. "1,3,5" (Mon,Wed,Fri)
 *  - EVERY_N_DAYS: the interval N as a string, e.g. "3"
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateTimeEpochMillis: Long,
    val repeatType: RepeatType,
    val repeatData: String? = null,
    val isActive: Boolean = true
)
