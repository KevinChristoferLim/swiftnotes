package com.example.wifty.repository

import com.example.wifty.data.api.*
import com.example.wifty.model.Note
import retrofit2.Response

class NotesRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    suspend fun getNotes(token: String): Response<List<Note>> =
        apiService.getNotes("Bearer $token")

    suspend fun createNote(token: String, title: String, content: String?, folderId: Int?) =
        apiService.createNote("Bearer $token", CreateNoteRequest(title, content, folderId))

    suspend fun updateNote(token: String, id: String, title: String?, content: String?, folderId: Int?) =
        apiService.updateNote("Bearer $token", id, UpdateNoteRequest(title, content, folderId))

    suspend fun deleteNote(token: String, id: String) =
        apiService.deleteNote("Bearer $token", id)

    suspend fun addCollaborator(token: String, noteId: String, email: String) =
        apiService.addCollaborator("Bearer $token", noteId, AddCollaboratorRequest(email))
}
