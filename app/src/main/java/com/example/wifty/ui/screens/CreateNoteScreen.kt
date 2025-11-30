package com.example.wifty.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.wifty.viewmodel.NotesViewModel

@Composable
fun CreateNoteScreen(
    viewModel: NotesViewModel,
    onCreated: (String) -> Unit
) {
    // Automatically create a note when entering this screen
    LaunchedEffect(Unit) {
        viewModel.createNote { newId ->
            onCreated(newId)   // Navigate to ViewNoteScreen(newId)
        }
    }
}
