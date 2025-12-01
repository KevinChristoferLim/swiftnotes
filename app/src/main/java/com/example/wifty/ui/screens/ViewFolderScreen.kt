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
import androidx.compose.ui.unit.dp
import com.example.wifty.model.Folder
import com.example.wifty.model.Note
import com.example.wifty.viewmodel.FolderViewModel
import com.example.wifty.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFolderScreen(
    folderId: String,
    folderVM: FolderViewModel,
    notesVM: NotesViewModel,
    onBack: () -> Unit
) {
    // Local folder state — will remain null until you wire a real lookup
    var folderState by remember { mutableStateOf<Folder?>(null) }

    // Subscribe to notes list from NotesViewModel
    val notes by notesVM.notes.collectAsState()

    // If you later add a method to folderVM to load folder by id, call it here.
    // Example (pseudo):
    // LaunchedEffect(folderId) {
    //    folderState = folderVM.getFolderById(folderId)
    // }
    //
    // At the moment we leave folderState null because your FolderViewModel doesn't expose a getter.

    // For filtering notes we will try to match by folder tag (if available).
    // Best-effort: if the folder exists and has a tag, filter by that tag.
    val filteredNotes = remember(notes, folderState, folderId) {
        val tag = folderState?.tag?.ifBlank { null }
        if (tag != null) {
            notes.filter { n ->
                n.title.contains(tag, ignoreCase = true)
                        || n.content.contains(tag, ignoreCase = true)
            }
        } else {
            // If we don't know the tag, try to find notes that mention the folderId (rare),
            // otherwise just show all notes (so the screen is useful)
            val byId = notes.filter { n ->
                n.title.contains(folderId, ignoreCase = true)
                        || n.content.contains(folderId, ignoreCase = true)
            }
            if (byId.isNotEmpty()) byId else notes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            folderState?.title ?: "Folder",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            folderState?.tag ?: "ID: ${folderId.take(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // placeholder for actions (edit folder / delete / more)
                }
            )
        }
    ) { inner ->

        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Header area that uses folder color if available
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = (folderState?.colorLong?.let { Color(it) } ?: Color(0xFFDDDDFF)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Text(
                        text = folderState?.title ?: "Folder",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = folderState?.tag ?: "Tag: —",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes in this folder yet.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredNotes) { note ->
                        FolderNoteRow(note = note, onClick = { /* navigate to note if needed */ })
                    }
                }
            }
        }
    }
}

/** Simple row representation for a note inside a folder view. */
@Composable
private fun FolderNoteRow(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title.ifBlank { "(Untitled)" }, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content.take(120).let { if (note.content.length > 120) "$it..." else it },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(note.updatedAt)),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
