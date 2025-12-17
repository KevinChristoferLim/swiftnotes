package com.example.wifty.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.wifty.ui.screens.login.AuthViewModel
import com.example.wifty.viewmodel.NotesViewModel

@Composable
fun CreateNoteScreen(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    onCreated: (String) -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    
    // Automatically create a note when entering this screen
    LaunchedEffect(authState.token) {
        authState.token?.let { token ->
            viewModel.createNote(token) { newId ->
                onCreated(newId)   // Navigate to ViewNoteScreen(newId)
            }
        }
    }
}
