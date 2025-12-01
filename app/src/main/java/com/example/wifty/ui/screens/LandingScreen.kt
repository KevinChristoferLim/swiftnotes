package com.example.wifty.ui.screens.notes // NOTE: Changed package to 'notes'

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wifty.viewmodel.NotesViewModel

@Composable
fun NotesListScreen( // ⬅️ RENAMED FUNCTION
    viewModel: NotesViewModel,
    onCreateNewNote: () -> Unit, // ⬅️ ADDED
    onOpenNote: (String) -> Unit, // ⬅️ ADDED
    onOpenFolders: () -> Unit, // ⬅️ ADDED
    onOpenProfile: () -> Unit // ⬅️ ADDED
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

        // -------------------------------------------------------------
        // NOTE: You need to replace this Empty State content with your
        // actual Notes List UI (like a LazyColumn) that uses the 'notes'
        // list and calls 'onOpenNote' when a note item is clicked.
        // -------------------------------------------------------------

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Empty state content (Temporarily retained for structure)
            Text(text = "Start Your Journey", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Every big step starts with a small step.\nNote your first idea and start your journey!",
                style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            FloatingActionButton(onClick = {
                // If this FAB is exclusively for creating a NEW note
                onCreateNewNote()
            }) {
                Text("+")
            }
        }

        // Example placeholders for the new buttons (You'd move these to an AppBar or BottomBar)
        Button(onClick = onOpenFolders, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) { Text("Folders") }
        Button(onClick = onOpenProfile, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { Text("Profile") }
    }
}