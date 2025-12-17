package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Note
import com.example.wifty.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class NotesViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    private var _token: String? = null

    val ownedNotes: StateFlow<List<Note>> = combine(_notes, _currentUserId) { notes: List<Note>, userId: String? ->
        if (userId == null) notes 
        else notes.filter { it.ownerId?.toString() == userId || it.ownerId == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedNotes: StateFlow<List<Note>> = combine(
        _notes, 
        _currentUserId, 
        _currentUserEmail
    ) { notes: List<Note>, userId: String?, email: String? ->
        emptyList<Note>()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentUser(id: String?, email: String?, token: String?) {
        _currentUserId.value = id
        _currentUserEmail.value = email
        _token = token
        if (token != null) loadNotes(token)
    }

    fun loadNotes(token: String) {
        _token = token
        viewModelScope.launch {
            try {
                val res = repo.getNotes(token)
                if (res.isSuccessful) {
                    _notes.value = res.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load notes: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun refreshNotes(token: String) = loadNotes(token)

    fun createNote(token: String, title: String = "", content: String? = "", folderId: Int? = null, onCreated: (String) -> Unit = {}) {
        // Validate title is not empty
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val res = repo.createNote(token, title, content, folderId)
                if (res.isSuccessful) {
                    loadNotes(token)
                    val body = res.body()
                    // Try different possible ID keys from backend
                    val newId = body?.get("id")?.toString() 
                        ?: body?.get("_id")?.toString() 
                        ?: body?.get("insertId")?.toString()
                        ?: ""
                    
                    if (newId.isNotEmpty()) {
                        onCreated(newId)
                    } else {
                        _error.value = "Note created, but no ID returned from server"
                    }
                } else {
                    _error.value = "Create failed: ${res.code()} - ${res.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun createNoteInFolder(token: String, folderId: String, onCreated: (String) -> Unit = {}) {
        val folderIdInt = folderId.toIntOrNull()
        createNote(token, folderId = folderIdInt, onCreated = onCreated)
    }

    fun updateNote(token: String, id: String, title: String?, content: String?, folderId: Int?) {
        viewModelScope.launch {
            try {
                val res = repo.updateNote(token, id, title, content, folderId)
                if (res.isSuccessful) {
                    loadNotes(token)
                } else {
                    _error.value = "Update failed: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun updateNote(token: String, note: Note) {
        updateNote(token, note.id, note.title, note.content, note.folderId)
    }

    fun deleteNote(token: String, id: String) {
        viewModelScope.launch {
            try {
                val res = repo.deleteNote(token, id)
                if (res.isSuccessful) {
                    loadNotes(token)
                } else {
                    _error.value = "Delete failed: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun addCollaborator(token: String, noteId: String, email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repo.addCollaborator(token, noteId, email)
                if (res.isSuccessful) {
                    loadNotes(token)
                    onSuccess()
                } else {
                    _error.value = "Add collaborator failed: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }
    
    fun addCollaboratorToNote(token: String, noteId: String, email: String) {
        addCollaborator(token, noteId, email)
    }

    fun getNoteById(noteId: String, onResult: (Note?) -> Unit) {
        val note = _notes.value.find { it.id == noteId }
        onResult(note)
    }

    fun copyNote(token: String, note: Note) {
        createNote(token, title = (note.title ?: "") + " (copy)", content = note.content, folderId = note.folderId)
    }

    fun moveNoteToFolder(token: String, noteId: String, folderId: Int?) {
        val note = _notes.value.find { it.id == noteId }
        if (note != null) {
            updateNote(token, noteId, note.title, note.content, folderId)
        }
    }

    fun addReminderToNote(token: String, noteId: String, reminder: Any) {}
    fun togglePin(token: String, noteId: String) {}
    fun toggleLock(token: String, noteId: String) {}

    fun clearError() { _error.value = null }
}
