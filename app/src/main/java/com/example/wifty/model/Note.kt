package com.example.wifty.model

import com.example.wifty.ui.screens.modules.ReminderData
import com.google.gson.annotations.SerializedName

data class Note(
    val id: String,
    val title: String = "",
    @SerializedName("description") val content: String = "",
    @SerializedName("owner_id") val ownerId: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("folder_id") val folderId: Int? = null,
    @SerializedName("is_locked") val isLocked: Boolean = false,
    @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("is_collaboration") val isCollaboration: Boolean = false,
    
    // UI specific fields preserved for app functionality
    @SerializedName("color_long")
    val colorLong: Long = 0xFF4B63FFu.toLong(),
    @SerializedName("is_pinned")
    val isPinned: Boolean = false,
    @SerializedName("checklist")
    val checklist: List<ChecklistItem> = emptyList(),
    @SerializedName("reminder")
    val reminder: ReminderData? = null
)

