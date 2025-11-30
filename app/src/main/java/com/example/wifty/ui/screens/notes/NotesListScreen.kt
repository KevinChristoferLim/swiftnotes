package com.example.wifty.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wifty.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onCreateNewNote: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val notes by viewModel.notes.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNewNote) {
                Text("+")
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {

            Text("Your Notes", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notes.size) { i ->
                    val n = notes[i]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(n.id) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(n.title.ifBlank { "(Untitled)" },
                                style = MaterialTheme.typography.titleMedium)

                            Spacer(Modifier.height(6.dp))

                            Text(n.content.take(60) + "...",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
