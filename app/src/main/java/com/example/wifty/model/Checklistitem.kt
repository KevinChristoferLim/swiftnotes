package com.example.wifty.model

data class ChecklistItem(
    val noteId: String = "",
    val text: String = "",
    val isChecked: Boolean = false,
    val order: Int = 0                  // For reordering items
)
