package com.example.wifty.repository

import com.example.wifty.data.api.RetrofitClient
import com.example.wifty.data.api.TokenStore
import com.example.wifty.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderRepository {

    // In-memory list of folders (kept for UI state flow, but sync with API)
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

    // CREATE
    suspend fun addFolder(folder: Folder) {
        val token = TokenStore.token ?: return
        try {
            val response = RetrofitClient.apiService.createFolder("Bearer $token", folder)
            if (response.isSuccessful) {
                refreshFolders()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // UPDATE whole folder object
    suspend fun updateFolder(updated: Folder) {
        val token = TokenStore.token ?: return
        try {
            val response = RetrofitClient.apiService.updateFolder("Bearer $token", updated.id, updated)
            if (response.isSuccessful) {
                refreshFolders()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // DELETE
    suspend fun deleteFolder(id: String) {
        val token = TokenStore.token ?: return
        try {
            val response = RetrofitClient.apiService.deleteFolder("Bearer $token", id)
            if (response.isSuccessful) {
                refreshFolders()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // UPDATE title only
    suspend fun updateFolderTitle(folderId: String, newTitle: String) {
        val folder = foldersData.value.find { it.id == folderId } ?: return
        updateFolder(folder.copy(title = newTitle))
    }

    // UPDATE description (tag)
    suspend fun updateFolderDescription(folderId: String, newDescription: String) {
        val folder = foldersData.value.find { it.id == folderId } ?: return
        updateFolder(folder.copy(tag = newDescription))
    }
}
