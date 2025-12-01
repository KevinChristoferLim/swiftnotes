package com.example.wifty.model


data class Reminder(
    val id: String = "",
    val noteId: String = "",
    val timestamp: Long = 0L,
    val reminderDay: ReminderDay = ReminderDay.None,
    val place: String? = null,
    val isActive: Boolean = true
)


enum class ReminderDay  {
    None, Everyday,
    Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
}
