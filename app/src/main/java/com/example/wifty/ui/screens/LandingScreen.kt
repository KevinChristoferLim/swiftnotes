package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wifty.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    viewModel: NotesViewModel,
    onCreateNote: (String) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFFEDE9FF), Color.White))
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Empty state content
            Text(text = "Start Your Journey", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Every big step starts with a small step.\nNote your first idea and start your journey!",
                style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            FloatingActionButton(onClick = {
                // create note and navigate on success
                viewModel.createNote { noteId ->
                    onCreateNote(noteId)
                }
            }) {
                Text("+")
            }
        }
    }
}
