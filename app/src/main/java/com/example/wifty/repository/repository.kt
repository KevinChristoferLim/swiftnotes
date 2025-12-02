    package com.example.wifty.repository

    import com.example.wifty.model.Note
    import java.util.UUID
    import kotlinx.coroutines.sync.Mutex
    import kotlinx.coroutines.sync.withLock

    /**
     * Minimal in-memory repository. Replace with Room/Firebase as needed.
     */
    class NotesRepository {

        private val mutex = Mutex()
        // simple in-memory map: id -> note
        private val notes = linkedMapOf<String, Note>()

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

            mutex.withLock { notes[id] = note }
            return note
        }

        suspend fun updateNote(note: Note) {
            mutex.withLock {
                notes[note.id] = note.copy(updatedAt = System.currentTimeMillis())
            }
        }

        suspend fun deleteNote(id: String) {
            mutex.withLock { notes.remove(id) }
        }

        suspend fun getNote(id: String): Note? {
            return mutex.withLock { notes[id] }
        }

        suspend fun getAllNotes(): List<Note> {
            return mutex.withLock { notes.values.toList().reversed() } // newest first
        }

        suspend fun insert(note: Note) {
            mutex.withLock {
                notes[note.id] = note
            }
        }

        suspend fun moveNoteToFolder(noteId: String, newfolderId: String?) {
            mutex.withLock {
                val note = notes[noteId] ?: return
                val updated = note.copy(
                    folderId = newfolderId,
                    updatedAt = System.currentTimeMillis()
                )
                notes[noteId] = updated
            }
        }

        suspend fun getNotesForFolder(folderId: String): List<Note> {
            return mutex.withLock {
                notes.values.filter { it.folderId == folderId }
            }
        }
    }
