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
 * Now includes seconds for better precision and allows passing current time for live updates.
 */
fun formatReminder(reminder: ReminderData?, now: Long = System.currentTimeMillis()): String? {
    if (reminder == null) return null
    val targetTime = reminder.timeMillis ?: return null
    val diff = targetTime - now

    if (diff < 0) return "Overdue"

    val totalSeconds = diff / 1000
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        days > 0 -> "In ${days}d ${hours}h"
        hours > 0 -> "In ${hours}h ${minutes}m"
        minutes > 0 -> "In ${minutes}m ${seconds}s"
        else -> "In ${seconds}s"
    }
}
