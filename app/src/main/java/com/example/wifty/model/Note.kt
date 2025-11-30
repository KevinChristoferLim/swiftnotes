package com.example.wifty.model

/**
 * Note model. color stored as Long (ARGB) for persistence / passing around.
 */
data class Note(
    val id: String,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorLong: Long = 0xFF4B63FFu.toLong(), // default accent as Long
    val isPinned: Boolean = false
)
