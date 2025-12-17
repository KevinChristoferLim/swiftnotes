package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Folder
import com.example.wifty.repository.FolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.flow.update

class FolderViewModel(
    private val repo: FolderRepository = FolderRepository()
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            repo.refreshFolders()
            repo.getFolders().collect { list ->
                _folders.value = list
            }
        }
    }

    fun createFolder(title: String, tag: String, colorLong: Long) {
        viewModelScope.launch {
            repo.addFolder(
                Folder(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    tag = tag,
                    colorLong = colorLong
                )
            )
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            repo.deleteFolder(id)
        }
    }

    fun renameFolder(folderId: String, newTitle: String) {
        viewModelScope.launch {
            repo.updateFolderTitle(folderId, newTitle)
        }
    }

    fun updateFolderDescription(folderId: String, newDescription: String) {
        viewModelScope.launch {
            repo.updateFolderDescription(folderId, newDescription)
        }
    }

    fun addNoteToFolder(folderId: String, noteId: String) {
        _folders.update { currentFolders ->
            currentFolders.map { folder ->
                if (folder.id == folderId) {
                    folder.copy(noteIds = folder.noteIds + noteId)
                } else folder
            }
        }
    }

    fun removeNoteFromFolder(folderId: String, noteId: String) {
        _folders.update { currentFolders ->
            currentFolders.map { folder ->
                if (folder.id == folderId) {
                    folder.copy(noteIds = folder.noteIds - noteId)
                } else folder
            }
        }
    }
}
