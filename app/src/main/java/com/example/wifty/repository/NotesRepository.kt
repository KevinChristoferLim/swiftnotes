package com.example.wifty.repository

import com.example.wifty.data.api.*
import com.example.wifty.model.Note
import retrofit2.Response

class NotesRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun getNotes(token: String): Response<List<Note>> =
        apiService.getNotes("Bearer $token")

    suspend fun createNote(token: String, title: String, content: String?, folderId: Int?,
                             reminderDateMillis: Long? = null,
                             reminderTimeMillis: Long? = null,
                             reminderRepeat: List<String>? = null,
                             reminderLocation: String? = null) =
        apiService.createNote("Bearer $token", CreateNoteRequest(
            title,
            content,
            folderId,
            reminderDateMillis,
            reminderTimeMillis,
            reminderRepeat,
            reminderLocation
        ))

    suspend fun updateNote(token: String, id: String, title: String?, content: String?, folderId: Int?,
                             reminderDateMillis: Long? = null,
                             reminderTimeMillis: Long? = null,
                             reminderRepeat: List<String>? = null,
                             reminderLocation: String? = null) =
        apiService.updateNote("Bearer $token", id, UpdateNoteRequest(
            title,
            content,
            folderId,
            reminderDateMillis,
            reminderTimeMillis,
            reminderRepeat,
            reminderLocation
        ))

    suspend fun deleteNote(token: String, id: String) =
        apiService.deleteNote("Bearer $token", id)

    suspend fun addCollaborator(token: String, noteId: String, email: String) =
        apiService.addCollaborator("Bearer $token", noteId, AddCollaboratorRequest(email))

    suspend fun removeCollaborator(token: String, noteId: String, collaboratorId: String) =
        apiService.removeCollaborator("Bearer $token", noteId, collaboratorId)
}
