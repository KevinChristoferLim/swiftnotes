package com.example.wifty.model

data class Reminder(
    val id: String = "",
    val noteId: String = "",
    val timestamp: Long = 0L,         // When reminder will fire
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val isActive: Boolean = true
)

// Types of repeat schedules
enum class RepeatInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
