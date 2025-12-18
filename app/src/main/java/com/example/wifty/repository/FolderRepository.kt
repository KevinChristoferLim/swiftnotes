package com.example.wifty.repository

import com.example.wifty.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderRepository {

    // In-memory list of folders
    private val foldersData = MutableStateFlow<List<Folder>>(emptyList())

    fun getFolders(): StateFlow<List<Folder>> = foldersData.asStateFlow()

    // CREATE
    suspend fun addFolder(folder: Folder) {
        foldersData.value = foldersData.value + folder
    }

    // UPDATE whole folder object
    suspend fun updateFolder(updated: Folder) {
        foldersData.value = foldersData.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    // DELETE
    suspend fun deleteFolder(id: String) {
        foldersData.value = foldersData.value.filterNot { it.id == id }
    }

    // UPDATE title only
    suspend fun updateFolderTitle(folderId: String, newTitle: String) {
        foldersData.value = foldersData.value.map {
            if (it.id == folderId) it.copy(title = newTitle) else it
        }
    }

    // UPDATE description (tag)
    suspend fun updateFolderDescription(folderId: String, newDescription: String) {
        foldersData.value = foldersData.value.map {
            if (it.id == folderId) it.copy(tag = newDescription) else it
        }
    }
}
