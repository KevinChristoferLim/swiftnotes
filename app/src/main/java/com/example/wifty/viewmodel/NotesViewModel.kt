package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Note
import com.example.wifty.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * NotesViewModel holds list of notes and provides create/update/delete operations.
 * Uses a simple in-memory NotesRepository. Replace repo implementation as needed.
 */
class NotesViewModel(
    private val repo: NotesRepository = NotesRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        // initial load (empty)
        refreshNotes()
    }

    fun refreshNotes() {
        viewModelScope.launch {
            _notes.value = repo.getAllNotes()
        }
    }

    /**
     * Creates a new blank note and returns its id via callback.
     */
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

    fun insertNote(note: Note) = viewModelScope.launch {
        repo.insert(note)
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


}
