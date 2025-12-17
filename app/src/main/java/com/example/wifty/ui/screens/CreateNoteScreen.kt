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
    folderId: String? = null,
    onCreated: (String) -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    
    LaunchedEffect(authState.token, folderId) {
        authState.token?.let { token ->
            if (folderId != null) {
                viewModel.createNoteInFolder(token, folderId) { newId ->
                    onCreated(newId)
                }
            } else {
                viewModel.createNote(token) { newId ->
                    onCreated(newId)
                }
            }
        }
    }
}
