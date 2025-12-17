package com.example.wifty.repository

import com.example.wifty.data.api.ApiService
import com.example.wifty.data.api.CollaboratorRequest
import com.example.wifty.data.api.RetrofitClient
import com.example.wifty.model.Note
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotesRepository(private val apiService: ApiService = RetrofitClient.apiService) {

    private val mutex = Mutex()
    // simple in-memory map: id -> note, serves as a cache
    private val notes = linkedMapOf<String, Note>()

    /**
     * Fetches all notes from the backend, updating the in-memory cache.
     * Falls back to the cache if the network request fails.
     */
    suspend fun getAllNotes(token: String): List<Note> {
        return try {
            val response = apiService.getNotes("Bearer $token")
            if (response.isSuccessful) {
                val networkNotes = response.body() ?: emptyList()
                mutex.withLock {
                    notes.clear()
                    networkNotes.forEach { notes[it.id] = it }
                }
                networkNotes
            } else {
                mutex.withLock { notes.values.toList().reversed() }
            }
        } catch (e: Exception) {
            mutex.withLock { notes.values.toList().reversed() }
        }
    }

    /**
     * Returns notes from the local cache.
     */
    suspend fun getAllNotesLocally(): List<Note> {
        return mutex.withLock { notes.values.toList().reversed() }
    }

    /**
     * Adds a collaborator to a note via the backend.
     */
    suspend fun addCollaborator(token: String, noteId: String, email: String) {
        apiService.addCollaborator("Bearer $token", noteId, CollaboratorRequest(email))
    }

    suspend fun createNote(
        token: String,
        initialTitle: String = "",
        initialContent: String = "",
        colorLong: Long = 0xFF4B63FFu.toLong(),
        folderId: String? = null,
        ownerId: String? = null
    ): Note? {
        val id = UUID.randomUUID().toString()
        val note = Note(
            id = id,
            title = initialTitle,
            content = initialContent,
            colorLong = colorLong,
            folderId = folderId,
            ownerId = ownerId
        )
        return try {
            val response = apiService.createNote("Bearer $token", note)
            if (response.isSuccessful) {
                val createdNote = response.body() ?: note
                mutex.withLock { notes[createdNote.id] = createdNote }
                createdNote
            } else {
                // Fallback to local if backend fails (optional strategy)
                mutex.withLock { notes[id] = note }
                note
            }
        } catch (e: Exception) {
            mutex.withLock { notes[id] = note }
            note
        }
    }

    suspend fun updateNote(token: String, note: Note) {
        try {
            val response = apiService.updateNote("Bearer $token", note.id, note)
            if (response.isSuccessful) {
                val updatedNote = response.body() ?: note
                mutex.withLock { notes[updatedNote.id] = updatedNote }
            } else {
                mutex.withLock {
                    notes[note.id] = note.copy(updatedAt = System.currentTimeMillis())
                }
            }
        } catch (e: Exception) {
            mutex.withLock {
                notes[note.id] = note.copy(updatedAt = System.currentTimeMillis())
            }
        }
    }

    suspend fun deleteNote(token: String, id: String) {
        try {
            val response = apiService.deleteNote("Bearer $token", id)
            if (response.isSuccessful) {
                mutex.withLock { notes.remove(id) }
            }
        } catch (e: Exception) {
            // Even if network fails, we might want to remove it locally or mark for deletion
            mutex.withLock { notes.remove(id) }
        }
    }

    suspend fun getNote(id: String): Note? {
        return mutex.withLock { notes[id] }
    }

    suspend fun insert(note: Note) {
        mutex.withLock {
            notes[note.id] = note
        }
    }

    suspend fun moveNoteToFolder(token: String, noteId: String, folderId: String?) {
        mutex.withLock {
            val note = notes[noteId] ?: return
            val updated = note.copy(
                folderId = folderId,
                updatedAt = System.currentTimeMillis()
            )
            updateNote(token, updated)
        }
    }

    suspend fun getNotesForFolder(folderId: String): List<Note> {
        return mutex.withLock {
            notes.values.filter { it.folderId == folderId }
        }
    }
}
