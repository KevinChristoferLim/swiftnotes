package com.example.wifty.repository

import com.example.wifty.model.Reminder
import com.example.wifty.model.ReminderDay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ReminderRepository {

    private val mutex = Mutex()

    // id -> Reminder
    private val reminders = linkedMapOf<String, Reminder>()

    /**
     * Create a fresh reminder
     */
    suspend fun createReminder(
        noteId: String,
        timestamp: Long,
        reminderDay: ReminderDay,
        place: String? = null,
        isActive: Boolean = true
    ): Reminder {
        val id = UUID.randomUUID().toString()
        val reminder = Reminder(
            id = id,
            noteId = noteId,
            timestamp = timestamp,
            reminderDay = reminderDay,
            place = place,
            isActive = isActive
        )

        mutex.withLock {
            reminders[id] = reminder
        }
        return reminder
    }

    /**
     * Update existing reminder
     */
    suspend fun updateReminder(reminder: Reminder) {
        mutex.withLock {
            reminders[reminder.id] = reminder
        }
    }

    /**
     * Delete reminder by ID
     */
    suspend fun deleteReminder(id: String) {
        mutex.withLock {
            reminders.remove(id)
        }
    }

    /**
     * Get reminder by ID
     */
    suspend fun getReminder(id: String): Reminder? {
        return mutex.withLock { reminders[id] }
    }

    /**
     * Get all reminders belonging to a specific note
     */
    suspend fun getRemindersByNoteId(noteId: String): List<Reminder> {
        return mutex.withLock {
            reminders.values.filter { it.noteId == noteId }
        }
    }

    /**
     * Get all reminders in the system
     */
    suspend fun getAllReminders(): List<Reminder> {
        return mutex.withLock { reminders.values.toList() }
    }

    /**
     * Insert a reminder with full control (overwrite)
     */
    suspend fun insert(reminder: Reminder) {
        mutex.withLock {
            reminders[reminder.id] = reminder
        }
    }
}
