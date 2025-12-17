package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Folder
import com.example.wifty.repository.FolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FolderViewModel(
    private val repo: FolderRepository = FolderRepository()
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private var _token: String? = null
    private var _currentUserId: String? = null

    fun setCurrentUser(id: String?, token: String?) {
        _currentUserId = id
        _token = token
    }

    fun refreshFolders() {
        _token?.let {
            viewModelScope.launch {
                repo.refreshFolders()
            }
        }
    }

    init {
        viewModelScope.launch {
            repo.getFolders().collect {
                _folders.value = it
            }
        }
    }

    fun createFolder(folder: Folder, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            repo.createFolder(folder)?.let { createdFolder ->
                onCreated(createdFolder.id)
            }
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            repo.deleteFolder(id)
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val folder = _folders.value.find { it.id == folderId }
            folder?.let {
                repo.updateFolder(it.copy(title = newName))
            }
        }
    }

    fun updateFolderDescription(folderId: String, newDescription: String?) {
        viewModelScope.launch {
            val folder = _folders.value.find { it.id == folderId }
            folder?.let {
                repo.updateFolder(it.copy(description = newDescription))
            }
        }
    }

    fun addNoteToFolder(folderId: String, noteId: String) {
        _token?.let { token ->
            viewModelScope.launch {
                repo.addNoteToFolder(token, folderId, noteId)
            }
        }
    }
}
