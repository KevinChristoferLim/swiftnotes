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

class NotesViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        refreshNotes()
    }

    fun refreshNotes() {
        viewModelScope.launch {
            _notes.value = repo.getAllNotes()
        }
    }

    fun createNote(onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val note = repo.createNote()
            refreshNotes()
            onCreated(note.id)
        }
    }

    fun getNoteById(noteId: String, onResult: (Note?) -> Unit) {
        viewModelScope.launch {
            onResult(repo.getNote(noteId))
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repo.updateNote(note)
            refreshNotes()
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repo.deleteNote(id)
            refreshNotes()
        }
    }

    fun copyNote(note: Note) {
        viewModelScope.launch {
            val newNote = note.copy(
                id = UUID.randomUUID().toString(),
                title = note.title + " (copy)",
                updatedAt = System.currentTimeMillis()
            )
            repo.insert(newNote)
            refreshNotes()
        }
    }

    fun togglePin(noteId: String) {
        val updatedNotes = _notes.value.map {
            if (it.id == noteId) it.copy(isPinned = !it.isPinned) else it
        }
        _notes.value = updatedNotes
        // You may want to update the repo here if persistence is needed
    }

    fun toggleLock(noteId: String) {
        val updatedNotes = _notes.value.map {
            if (it.id == noteId) it.copy(isLocked = !it.isLocked) else it
        }
        _notes.value = updatedNotes
        // You may want to update the repo here if persistence is needed
    }

    fun setReminder(noteId: String, timeInMillis: Long) {
        // Implement Reminder logic using WorkManager here
    }

    fun attachImageToNote(noteId: String, uri: Uri) {
        val note = _notes.value.find { it.id == noteId } ?: return
        val updatedContent = note.content + "\n[Image Attached: $uri]"
        updateNote(note.copy(content = updatedContent))
    }

    fun attachFileToNote(noteId: String, uri: Uri) {
        val note = _notes.value.find { it.id == noteId } ?: return
        val updatedContent = note.content + "\n[File Attached: $uri]"
        updateNote(note.copy(content = updatedContent))
    }

    fun moveNoteToFolder(noteId: String, folderId: String?) {
        viewModelScope.launch {
            repo.moveNoteToFolder(noteId, folderId)
            refreshNotes()
        }
    }

    fun createNoteInFolder(
        folderId: String,
        onCreated: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val note = repo.createNote(
                initialTitle = "",
                initialContent = "",
                colorLong = 0xFF4B63FF,
            ).copy(folderId = folderId)

            repo.updateNote(note)
            refreshNotes()

            onCreated(note.id)
        }
    }


}