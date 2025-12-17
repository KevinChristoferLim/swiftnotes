package com.example.wifty.repository

import com.example.wifty.data.api.RetrofitClient
import com.example.wifty.data.api.TokenStore
import com.example.wifty.model.Reminder
import com.example.wifty.model.ReminderDay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ReminderRepository {

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

        val token = TokenStore.token
        if (token != null) {
            try {
                val response = RetrofitClient.apiService.createReminder("Bearer $token", reminder)
                if (response.isSuccessful) {
                    return response.body() ?: reminder
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return reminder
    }

    /**
     * Update existing reminder
     */
    suspend fun updateReminder(reminder: Reminder) {
        val token = TokenStore.token ?: return
        try {
            RetrofitClient.apiService.updateReminder("Bearer $token", reminder.id, reminder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete reminder by ID
     */
    suspend fun deleteReminder(id: String) {
        val token = TokenStore.token ?: return
        try {
            RetrofitClient.apiService.deleteReminder("Bearer $token", id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get reminder by ID
     */
    suspend fun getReminder(id: String): Reminder? {
        val token = TokenStore.token ?: return null
        try {
            val response = RetrofitClient.apiService.getReminder("Bearer $token", id)
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Get all reminders belonging to a specific note
     */
    suspend fun getRemindersByNoteId(noteId: String): List<Reminder> {
        val token = TokenStore.token ?: return emptyList()
        try {
            val response = RetrofitClient.apiService.getRemindersByNoteId("Bearer $token", noteId)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    /**
     * Get all reminders in the system
     */
    suspend fun getAllReminders(): List<Reminder> {
        val token = TokenStore.token ?: return emptyList()
        try {
            val response = RetrofitClient.apiService.getAllReminders("Bearer $token")
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    /**
     * Insert a reminder with full control (overwrite)
     */
    suspend fun insert(reminder: Reminder) {
        val token = TokenStore.token ?: return
        try {
            RetrofitClient.apiService.createReminder("Bearer $token", reminder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
