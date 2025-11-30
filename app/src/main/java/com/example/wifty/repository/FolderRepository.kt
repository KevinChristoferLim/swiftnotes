package com.example.wifty.repository

import com.example.wifty.model.Folder
import kotlinx.coroutines.flow.Flow

class FolderRepository {

    private val folders = mutableListOf<Folder>()

    fun getAllFolders(): List<Folder> = folders

    fun addFolder(folder: Folder) {
        folders.add(folder)
    }

    fun deleteFolder(id: String) {
        folders.removeAll { it.id == id }
    }
}

