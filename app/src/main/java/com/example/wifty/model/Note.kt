package com.example.wifty.model

/**
 * Note model. color stored as Long (ARGB) for persistence / passing around.
 */
data class Note(
    val id: String,
    val title: String = "",
    val content: String = "",
    val folderId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorLong: Long = 0xFF4B63FFu.toLong(),
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val checklist: List<ChecklistItem> = emptyList()
)


