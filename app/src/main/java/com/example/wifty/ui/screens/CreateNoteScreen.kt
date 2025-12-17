package com.example.wifty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val error by viewModel.error.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Create New Note", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title (required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (error != null) {
            Text("Error: $error", color = Color.Red, modifier = Modifier.padding(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Button(
            onClick = {
                authState.token?.let { token ->
                    viewModel.createNote(token, title, content) { newId ->
                        if (newId.isNotEmpty()) {
                            onCreated(newId)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = title.isNotBlank() && authState.token != null
        ) {
            Text("Create Note")
        }
    }
}
