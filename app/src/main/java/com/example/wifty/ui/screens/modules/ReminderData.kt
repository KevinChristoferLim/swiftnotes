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
