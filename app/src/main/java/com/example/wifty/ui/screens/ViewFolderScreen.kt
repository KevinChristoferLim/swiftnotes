package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wifty.model.Note
import com.example.wifty.viewmodel.FolderViewModel
import com.example.wifty.viewmodel.NotesViewModel
import com.example.wifty.ui.screens.modules.TopNavBarWithBack
import com.example.wifty.ui.screens.login.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFolderScreen(
    folderId: String,
    folderVM: FolderViewModel,
    notesVM: NotesViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val folders by folderVM.folders.collectAsState()
    val folder = remember(folders, folderId) { folders.find { it.id == folderId } }

    val allNotes by notesVM.notes.collectAsState()

    //  Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("Notes") }

    // Filter notes belonging to this folder
    val notesInFolder = remember(allNotes, folder) {
        folder?.let { f -> allNotes.filter { f.noteIds.contains(it.id) } } ?: emptyList()
    }

    // Apply search filter (same logic as FolderListScreen)
    val filteredNotes = remember(notesInFolder, searchQuery) {
        if (searchQuery.isBlank()) notesInFolder
        else notesInFolder.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopNavBarWithBack(
                title = folder?.title ?: "Folder",
                subtitle = folder?.tag ?: "Tag: —",
                onBack = onBack,
                showProfile = true,
                onOpenProfile = onOpenProfile,
                onSearchClick = { query, type ->
                    searchQuery = query
                    searchType = "Notes"
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ---------------------------------------------------
            // Folder Header Card
            // ---------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(folder?.colorLong?.let { Color(it) } ?: Color(0xFFDDEEFF))
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            folder?.title ?: "Folder",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            folder?.tag ?: "Tag: —",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${filteredNotes.size} notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------------------------------------------------
            // Notes List (or Empty State)
            // ---------------------------------------------------
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No notes found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredNotes) { note ->
                        FolderNoteRow(note) { onOpenNote(note.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderNoteRow(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                note.title.ifBlank { "(Untitled)" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = note.content.take(120).let { if (note.content.length > 120) "$it..." else it },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
