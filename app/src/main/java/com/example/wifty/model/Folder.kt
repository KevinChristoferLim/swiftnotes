package com.example.wifty.model

data class Folder(
    val id: String = "",        // Use String ID for consistency with Note
    val title: String = "",
    val tag: String = "",
    val colorLong: Long = 0xFF4B63FF // Store color as Long (ARGB)
)
