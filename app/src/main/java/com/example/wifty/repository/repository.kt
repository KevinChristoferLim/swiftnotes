package com.example.wifty.repository

import com.example.wifty.data.api.RetrofitClient
import com.example.wifty.data.api.TokenStore
import com.example.wifty.model.Note
import java.util.UUID

/**
 * Repository connected to API.
 */
class NotesRepository {

    suspend fun createNote(
        initialTitle: String = "",
        initialContent: String = "",
        colorLong: Long = 0xFF4B63FFu.toLong(),
        folderId: String? = null
    ): Note {
        val id = UUID.randomUUID().toString()
        val note = Note(
            id = id,
            title = initialTitle,
            content = initialContent,
            colorLong = colorLong,
            folderId = folderId
        )

        val token = TokenStore.token
        if (token != null) {
            try {
                val response = RetrofitClient.apiService.createNote("Bearer $token", note)
                if (response.isSuccessful) {
                    return response.body() ?: note
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return note
    }

    suspend fun updateNote(note: Note) {
        val token = TokenStore.token ?: return
        try {
            RetrofitClient.apiService.updateNote("Bearer $token", note.id, note)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteNote(id: String) {
        val token = TokenStore.token ?: return
        try {
            RetrofitClient.apiService.deleteNote("Bearer $token", id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getNote(id: String): Note? {
        val token = TokenStore.token ?: return null
        try {
            val response = RetrofitClient.apiService.getNote("Bearer $token", id)
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun getAllNotes(): List<Note> {
        val token = TokenStore.token ?: return emptyList()
        try {
            val response = RetrofitClient.apiService.getAllNotes("Bearer $token")
            if (response.isSuccessful) {
                // Return newest first
                return response.body()?.sortedByDescending { it.updatedAt } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    suspend fun insert(note: Note) {
        val token = TokenStore.token ?: return
        try {
            RetrofitClient.apiService.createNote("Bearer $token", note)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun moveNoteToFolder(noteId: String, newfolderId: String?) {
        val note = getNote(noteId) ?: return
        val updated = note.copy(
            folderId = newfolderId,
            updatedAt = System.currentTimeMillis()
        )
        updateNote(updated)
    }

    suspend fun getNotesForFolder(folderId: String): List<Note> {
        return getAllNotes().filter { it.folderId == folderId }
    }
}
