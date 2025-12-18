package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Note
import com.example.wifty.repository.NotesRepository
import com.google.gson.Gson
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

    val allNotes: StateFlow<List<Note>> = combine(_notes, _currentUserId) { notes, userId ->
        if (userId == null) {
            notes
        } else {
            val uid = userId.toIntOrNull()
            if (uid == null) {
                notes
            } else {
                notes.filter { it.ownerId == uid || it.isCollaboration }
            }
        }
    }.combine(notes) { filteredNotes, allNotes ->
        allNotes.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
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

    fun createNote(token: String, title: String = "", content: String? = "", folderId: Int? = null, reminder: com.example.wifty.ui.screens.modules.ReminderData? = null, onCreated: (String) -> Unit = {}) {
        // Validate title is not empty
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val res = repo.createNote(token, title, content, folderId,
                    reminderDateMillis = reminder?.dateMillis,
                    reminderTimeMillis = reminder?.timeMillis,
                    reminderRepeat = reminder?.repeat,
                    reminderLocation = reminder?.location
                )
                if (res.isSuccessful) {
                    loadNotes(token)
                    val body = res.body()

                    // Normalize id values coming back from the backend (could be numeric types)
                    val rawId = body?.get("id") ?: body?.get("_id") ?: body?.get("insertId")
                    val newId = when (rawId) {
                        is Number -> {
                            // prefer integer string when possible
                            if (rawId.toDouble() % 1.0 == 0.0) rawId.toLong().toString() else rawId.toString()
                        }
                        else -> rawId?.toString() ?: ""
                    }

                    // If backend returned the created note object, insert it into local list
                    val gson = Gson()
                    val noteObject = body?.get("note")
                    if (noteObject != null) {
                        try {
                            val json = gson.toJson(noteObject)
                            val createdNote = gson.fromJson(json, Note::class.java)
                            // Normalize created note id if possible
                            val normalizedId = when (val raw = noteObject as? Map<*, *>) {
                                null -> createdNote.id
                                else -> {
                                    val nid = raw["id"]
                                    when (nid) {
                                        is Number -> if (nid.toDouble() % 1.0 == 0.0) nid.toLong().toString() else nid.toString()
                                        else -> nid?.toString() ?: createdNote.id
                                    }
                                }
                            }
                            val noteWithNormalizedId = if (createdNote.id != normalizedId) createdNote.copy(id = normalizedId) else createdNote

                            // Add to local list if not present
                            if (_notes.value.none { it.id == noteWithNormalizedId.id }) {
                                _notes.value = _notes.value + noteWithNormalizedId
                            }
                        } catch (e: Exception) {
                            // ignore parsing errors and fall back to reload
                        }
                    } else if (newId.isNotEmpty()) {
                        // If we only got an id, but no note object, synthesize a minimal note so UI can show it
                        val synthesizedId = if (newId.endsWith(".0")) newId.removeSuffix(".0") else newId
                        val synthesizedNote = Note(
                            id = synthesizedId,
                            title = title,
                            content = content ?: "",
                            ownerId = null,
                            userId = null,
                            folderId = folderId
                        )
                        if (_notes.value.none { it.id == synthesizedNote.id }) {
                            _notes.value = _notes.value + synthesizedNote
                        }
                    }

                    if (!newId.isNullOrEmpty()) {
                        onCreated(newId)
                    } else {
                        _error.value = "Note created, but no ID returned from server"
                    }
                } else {
                    // Map specific server responses (423 -> locked note) to friendly messages
                    _error.value = when (res.code()) {
                        423 -> "You cannot share locked notes"
                        else -> "Add collaborator failed: ${res.code()} - ${res.message()}"
                    }
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

    fun updateNote(token: String, id: String, title: String?, content: String?, folderId: Int?, isPinned: Boolean? = null) {
        viewModelScope.launch {
            try {
                val res = repo.updateNote(token, id, title, content, folderId, isPinned)
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
        updateNote(token, note.id, note.title, note.content, note.folderId, note.isPinned)
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

    fun removeCollaborator(token: String, noteId: String, collaboratorId: String) {
        viewModelScope.launch {
            try {
                val res = repo.removeCollaborator(token, noteId, collaboratorId)
                if (res.isSuccessful) {
                    loadNotes(token)
                } else {
                    _error.value = "Remove collaborator failed: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }


    fun lockNote(token: String, noteId: String, pin: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repo.lockNote(token, noteId, pin)
                if (res.isSuccessful) {
                    loadNotes(token)
                    onSuccess()
                } else {
                    val msg = "Lock failed: ${res.code()} - ${res.message()}"
                    _error.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = "Network error: ${e.message}"
                _error.value = msg
                onError(msg)
            }
        }
    }

    fun unlockNote(token: String, noteId: String, pin: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repo.unlockNote(token, noteId, pin)
                if (res.isSuccessful) {
                    loadNotes(token)
                    onSuccess()
                } else {
                    val msg = "Unlock failed: ${res.code()} - ${res.message()}"
                    _error.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = "Network error: ${e.message}"
                _error.value = msg
                onError(msg)
            }
        }
    }

    fun viewLockedNote(token: String, noteId: String, pin: String, onSuccess: (Map<String, Any>?) -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = repo.viewLockedNote(token, noteId, pin)
                if (res.isSuccessful) {
                    val body = res.body()
                    val noteObj = body?.get("note")
                    if (noteObj is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        onSuccess(noteObj as Map<String, Any>)
                    } else {
                        onSuccess(null)
                    }
                } else {
                    val msg = "PIN verify failed: ${res.code()} - ${res.message()}"
                    _error.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                val msg = "Network error: ${e.message}"
                _error.value = msg
                onError(msg)
            }
        }
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
    fun addReminderToNote(token: String, noteId: String, reminder: com.example.wifty.ui.screens.modules.ReminderData) {
        viewModelScope.launch {
            try {
                val res = repo.updateNote(
                    token,
                    noteId,
                    title = null,
                    content = null,
                    folderId = null,
                    reminderDateMillis = reminder.dateMillis,
                    reminderTimeMillis = reminder.timeMillis,
                    reminderRepeat = reminder.repeat,
                    reminderLocation = reminder.location
                )

                if (res.isSuccessful) {
                    loadNotes(token)
                } else {
                    _error.value = "Failed to save reminder: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }
    fun togglePin(token: String, noteId: String) {
        val note = _notes.value.find { it.id == noteId }
        if (note != null) {
            updateNote(token, noteId, note.title, note.content, note.folderId, !note.isPinned)
        }
    }
    fun toggleLock(token: String, noteId: String) {}

    fun clearError() { _error.value = null }
}
