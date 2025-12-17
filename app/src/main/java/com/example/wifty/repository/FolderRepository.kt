package com.example.wifty.repository

import com.example.wifty.data.api.RetrofitClient
import com.example.wifty.data.api.TokenStore
import com.example.wifty.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderRepository {

    private val foldersData = MutableStateFlow<List<Folder>>(emptyList())

    fun getFolders(): StateFlow<List<Folder>> = foldersData.asStateFlow()

    suspend fun refreshFolders() {
        val token = TokenStore.token ?: return
        try {
            val response = RetrofitClient.apiService.getAllFolders("Bearer $token")
            if (response.isSuccessful) {
                foldersData.value = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createFolder(folder: Folder): Folder? {
        val token = TokenStore.token ?: return null
        return try {
            val response = RetrofitClient.apiService.createFolder("Bearer $token", folder)
            if (response.isSuccessful) {
                val newFolder = response.body()
                if (newFolder != null) {
                    foldersData.value = foldersData.value + newFolder
                } else {
                    refreshFolders()
                }
                newFolder
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateFolder(updated: Folder) {
        val token = TokenStore.token ?: return
        try {
            val response = RetrofitClient.apiService.updateFolder("Bearer $token", updated.id, updated)
            if (response.isSuccessful) {
                val updatedFolder = response.body()
                if (updatedFolder != null) {
                    foldersData.value = foldersData.value.map {
                        if (it.id == updatedFolder.id) updatedFolder else it
                    }
                } else {
                    refreshFolders()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteFolder(id: String) {
        val token = TokenStore.token ?: return
        try {
            val response = RetrofitClient.apiService.deleteFolder("Bearer $token", id)
            if (response.isSuccessful) {
                foldersData.value = foldersData.value.filterNot { it.id == id }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun addNoteToFolder(token: String, folderId: String, noteId: String) {
        try {
            val response = RetrofitClient.apiService.addNoteToFolder("Bearer $token", folderId, noteId)
             if (response.isSuccessful) {
                foldersData.value = foldersData.value.map {
                    if (it.id == folderId) {
                        it.copy(noteIds = it.noteIds + noteId)
                    } else {
                        it
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
