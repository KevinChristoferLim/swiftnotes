package com.example.wifty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Folder
import com.example.wifty.repository.FolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FolderViewModel(
    private val repo: FolderRepository = FolderRepository()
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            repo.getFolders().collect { list ->
                _folders.value = list
            }
        }
    }

    fun createFolder(title: String, tag: String, colorLong: Long) {
        viewModelScope.launch {
            repo.addFolder(
                Folder(
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
}
