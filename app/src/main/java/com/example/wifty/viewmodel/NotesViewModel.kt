package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Note
import com.example.wifty.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import android.net.Uri
import com.example.wifty.ui.screens.modules.ReminderData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotesViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    private var _token: String? = null

    val ownedNotes: StateFlow<List<Note>> = combine(_notes, _currentUserId) { notes, userId ->
        if (userId == null) notes else notes.filter { it.ownerId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedNotes: StateFlow<List<Note>> = combine(_notes, _currentUserId, _currentUserEmail) { notes, userId, email ->
        if (userId == null || email == null) emptyList() 
        else notes.filter { it.ownerId != userId && it.collaborators.contains(email) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentUser(id: String?, email: String?, token: String?) {
        _currentUserId.value = id
        _currentUserEmail.value = email
        _token = token
    }

    fun refreshNotes(token: String) {
        _token = token
        viewModelScope.launch {
            _notes.value = repo.getAllNotes(token)
        }
    }

    fun addCollaboratorToNote(token: String, noteId: String, email: String) {
        viewModelScope.launch {
            repo.addCollaborator(token, noteId, email)
            refreshNotes(token) 
        }
    }

    fun createNote(token: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val note = repo.createNote(
                token = token,
                ownerId = _currentUserId.value
            )
            if (note != null) {
                refreshNotesLocally()
                onCreated(note.id)
            }
        }
    }

    private fun refreshNotesLocally() {
        viewModelScope.launch {
            _notes.value = repo.getAllNotesLocally() 
        }
    }

    fun getNoteById(noteId: String, onResult: (Note?) -> Unit) {
        viewModelScope.launch {
            onResult(repo.getNote(noteId))
        }
    }

    fun updateNote(token: String, note: Note) {
        viewModelScope.launch {
            repo.updateNote(token, note)
            refreshNotesLocally()
        }
    }

    fun deleteNote(token: String, id: String) {
        viewModelScope.launch {
            repo.deleteNote(token, id)
            refreshNotesLocally()
        }
    }

    fun copyNote(token: String, note: Note) {
        viewModelScope.launch {
            val newNote = note.copy(
                id = UUID.randomUUID().toString(),
                title = note.title + " (copy)",
                updatedAt = System.currentTimeMillis(),
                ownerId = _currentUserId.value
            )
            repo.insert(newNote)
            repo.createNote(token, newNote.title, newNote.content, newNote.colorLong, newNote.folderId, newNote.ownerId)
            refreshNotes(token)
        }
    }

    fun togglePin(token: String, noteId: String) {
        viewModelScope.launch {
            val note = repo.getNote(noteId)
            if (note != null) {
                updateNote(token, note.copy(isPinned = !note.isPinned))
            }
        }
    }

    fun toggleLock(token: String, noteId: String) {
        viewModelScope.launch {
             val note = repo.getNote(noteId)
             if (note != null) {
                 updateNote(token, note.copy(isLocked = !note.isLocked))
             }
        }
    }

    fun addReminderToNote(token: String, noteId: String, reminder: ReminderData) {
        viewModelScope.launch {
            val note = repo.getNote(noteId)
            if (note != null) {
                val updatedNote = note.copy(reminder = reminder, updatedAt = System.currentTimeMillis())
                repo.updateNote(token, updatedNote)
                refreshNotesLocally()
            }
        }
    }

    fun attachImageToNote(token: String, noteId: String, uri: Uri) {
        viewModelScope.launch {
            val note = repo.getNote(noteId) ?: return@launch
            val updatedContent = note.content + "\n[[IMAGE::${uri}]]"
            updateNote(
                token,
                note.copy(
                    content = updatedContent,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun attachFileToNote(token: String, noteId: String, uri: Uri) {
        viewModelScope.launch {
            val note = repo.getNote(noteId) ?: return@launch
            val updatedContent = note.content + "\n[[FILE::${uri}]]"
            updateNote(
                token,
                note.copy(
                    content = updatedContent,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun moveNoteToFolder(token: String, noteId: String, folderId: String?) {
        viewModelScope.launch {
            repo.moveNoteToFolder(token, noteId, folderId)
            refreshNotesLocally()
        }
    }

    fun createNoteInFolder(token: String, folderId: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val note = repo.createNote(
                token = token,
                initialTitle = "",
                initialContent = "",
                colorLong = 0xFF4B63FFu.toLong(),
                folderId = folderId,
                ownerId = _currentUserId.value
            )
            if (note != null) {
                refreshNotesLocally()
                onCreated(note.id)
            }
        }
    }
}
