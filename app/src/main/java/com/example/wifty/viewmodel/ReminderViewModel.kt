package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Reminder
import com.example.wifty.model.ReminderDay
import com.example.wifty.repository.ReminderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReminderState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReminderViewModel(
    private val repo: ReminderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderState())
    val state: StateFlow<ReminderState> = _state.asStateFlow()

    private val _reminderList = MutableStateFlow<List<Reminder>>(emptyList())
    val reminderList: StateFlow<List<Reminder>> = _reminderList.asStateFlow()

    fun loadReminder(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = repo.getReminder(id)
            _state.value = _state.value.copy(
                reminder = result,
                isLoading = false
            )
        }
    }

    fun loadRemindersForNote(noteId: String) {
        viewModelScope.launch {
            _reminderList.value = repo.getRemindersByNoteId(noteId)
        }
    }

    fun createReminder(
        noteId: String,
        timestamp: Long,
        reminderDay: ReminderDay = ReminderDay.None,
        place: String? = null
    ) {
        viewModelScope.launch {
            val reminder = repo.createReminder(
                noteId = noteId,
                timestamp = timestamp,
                reminderDay = reminderDay,
                place = place,
                isActive = true
            )

            _state.value = _state.value.copy(reminder = reminder)

            loadRemindersForNote(noteId)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repo.updateReminder(reminder)
            _state.value = _state.value.copy(reminder = reminder)

            loadRemindersForNote(reminder.noteId)
        }
    }

    fun deleteReminder(id: String, noteId: String) {
        viewModelScope.launch {
            repo.deleteReminder(id)
            _state.value = _state.value.copy(reminder = null)

            loadRemindersForNote(noteId)
        }
    }
}
