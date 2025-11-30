package com.example.wifty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.model.Folder
import com.example.wifty.repository.FolderRepository
import kotlinx.coroutines.launch

class FolderViewModel(
    private val repo: FolderRepository = FolderRepository()
) : ViewModel() {

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
}
