package com.example.wifty.ui.screens.modules

import com.google.gson.annotations.SerializedName

/**
 * ReminderData represents a reminder attached to a note.
 * These fields are serialized to/from JSON with snake_case naming to match the MySQL database.
 */
data class ReminderData(
    @SerializedName("reminder_date_millis")
    val dateMillis: Long? = null,
    
    @SerializedName("reminder_time_millis")
    val timeMillis: Long? = null,
    
    @SerializedName("reminder_repeat")
    val repeat: List<String>? = null,
    
    @SerializedName("reminder_location")
    val location: String? = null
)

/**
 * Formats a reminder into a human-readable countdown or status string.
 */
fun formatReminder(reminder: ReminderData?): String? {
    if (reminder == null) return null
    val targetTime = reminder.timeMillis ?: return null
    val now = System.currentTimeMillis()
    val diff = targetTime - now

    if (diff < 0) return "Overdue"

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "In $days d"
        hours > 0 -> "In $hours h"
        minutes > 0 -> "In $minutes m"
        else -> "Just now"
    }
}
