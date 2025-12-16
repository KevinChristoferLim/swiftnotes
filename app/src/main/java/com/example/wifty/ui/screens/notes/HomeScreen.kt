package com.example.wifty.ui.screens.notes

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.wifty.viewmodel.NotesViewModel
import com.example.wifty.viewmodel.FolderViewModel

@Composable
fun HomeScreen(
    notesVM: NotesViewModel,
    folderVM: FolderViewModel,
    onCreateNewNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenFolders: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val notes by notesVM.notes.collectAsState()

    // Key logic: If no notes -> show Landing. If there are notes -> show NotesList.
    if (notes.isNullOrEmpty()) {
        LandingScreen(
            onCreateNewNote = onCreateNewNote,
            onOpenFolders = onOpenFolders,
            onOpenProfile = onOpenProfile
        )
    } else {
        NotesListScreen(
            viewModel = notesVM,
            folderViewModel = folderVM,
            onCreateNewNote = onCreateNewNote,
            onOpenNote = onOpenNote,
            onOpenFolders = onOpenFolders,
            onOpenProfile = onOpenProfile
        )
    }
}
