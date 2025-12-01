package com.example.wifty.repository

import com.example.wifty.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderRepository {

    // In-Memory Data (Acts Like Database)
    private val foldersData = MutableStateFlow<List<Folder>>(emptyList())

    // This is what the ViewModel is trying to call
    fun getFolders(): StateFlow<List<Folder>> = foldersData.asStateFlow()

    // CRUD OPERATIONS

    suspend fun addFolder(folder: Folder) {
        foldersData.value = foldersData.value + folder
    }

    suspend fun updateFolder(updated: Folder) {
        foldersData.value = foldersData.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    suspend fun deleteFolder(id: String) {
        foldersData.value = foldersData.value.filterNot { it.id == id }
    }
}
