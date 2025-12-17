package com.example.wifty.model

import com.google.gson.annotations.SerializedName

data class Folder(
    @SerializedName("_id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("color")
    val colorLong: Long,
    @SerializedName("notes")
    val noteIds: List<String> = emptyList(),
    val tag: String? = null,
    val notesAmount: Int = 0,
)
