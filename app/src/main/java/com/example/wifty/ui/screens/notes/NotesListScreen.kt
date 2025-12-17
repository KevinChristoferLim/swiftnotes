package com.example.wifty.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import com.example.wifty.model.Note
import com.example.wifty.ui.screens.login.AuthViewModel
import com.example.wifty.ui.screens.modules.TopNavBar
import com.example.wifty.viewmodel.FolderViewModel
import com.example.wifty.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// --- Helper: Relative time ---
fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) ->
            "${diff / TimeUnit.MINUTES.toMillis(1)} minutes ago"
        diff < TimeUnit.DAYS.toMillis(1) ->
            "${diff / TimeUnit.HOURS.toMillis(1)} hours ago"
        diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

// --- Notes Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    folderViewModel: FolderViewModel,
    onCreateNewNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenFolders: () -> Unit,
    onOpenSharedNotes: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    
    // Update current user and refresh notes in NotesViewModel
    LaunchedEffect(authState.user, authState.token) {
        viewModel.setCurrentUser(authState.user?.id, authState.user?.email, authState.token)
        authState.token?.let { viewModel.refreshNotes(it) }
    }

    val ownedNotes by viewModel.ownedNotes.collectAsState()
    val folders by folderViewModel.folders.collectAsState()

    // --- Search State ---
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("Notes") }

    var menuExpanded by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    val filteredOwnedNotes = if (searchType == "Notes" && searchQuery.isNotBlank()) {
        ownedNotes.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else ownedNotes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF4EDFF), Color.White)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateNewNote,
                    containerColor = Color(0xFF4B63FF),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
                }
            }
        ) { innerPadding ->

            Column(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // --- Modular Top Bar ---
                TopNavBar(
                    searchType = "Notes",
                    onSearchClick = { query, type ->
                        searchQuery = query
                        searchType = "Notes"
                    },
                    onOpenFolders = onOpenFolders,
                    onOpenSharedNotes = onOpenSharedNotes,
                    onOpenProfile = onOpenProfile
                )

                Spacer(Modifier.height(16.dp))

                // --- Notes Grid ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- My Notes Section ---
                    if (filteredOwnedNotes.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                "My Notes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(filteredOwnedNotes) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onOpenNote(note.id) },
                                onLongPress = { pressOffset ->
                                    selectedNote = note
                                    menuOffset = pressOffset
                                    menuExpanded = true
                                }
                            )
                        }
                    }

                    if (filteredOwnedNotes.isEmpty()) {
                         item(span = { GridItemSpan(2) }) {
                             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                 Text("No notes found", color = Color.Gray)
                             }
                         }
                    }
                }
            }
        }

        // --- Popup Menu ---
        if (menuExpanded && selectedNote != null) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(menuOffset.x.toInt(), menuOffset.y.toInt()),
                onDismissRequest = { menuExpanded = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        Modifier
                            .padding(8.dp)
                            .width(150.dp)
                    ) {
                        Text(
                            "Delete",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val token = authState.token
                                    if (token != null) {
                                        viewModel.deleteNote(token, selectedNote!!.id)
                                    }
                                    menuExpanded = false
                                }
                                .padding(12.dp)
                        )

                        Text(
                            "Move to folder",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuExpanded = false
                                    showMoveDialog = true
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }

        // --- Move to Folder Dialog ---
        if (showMoveDialog && selectedNote != null) {
            Dialog(onDismissRequest = { showMoveDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .widthIn(min = 260.dp)
                    ) {
                        Text("Move Note To...", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))

                        if (folders.isEmpty()) {
                            Text("No folders available.", color = Color.Gray)
                        } else {
                            folders.forEach { folder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedNote?.let { note ->
                                                folderViewModel.addNoteToFolder(folder.id, note.id)
                                            }
                                            showMoveDialog = false
                                        }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .size(14.dp)
                                            .background(Color(folder.colorLong), RoundedCornerShape(4.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(folder.title)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = { showMoveDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongPress: (Offset) -> Unit
) {
    val timeLabel = remember(note.updatedAt) { formatRelativeTime(note.updatedAt) }
    val backgroundColor = if (note.colorLong == 0L) Color(0xFFD3E3FD) else Color(note.colorLong)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { offset -> onLongPress(offset) }
                )
            }
            .background(backgroundColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(Modifier.fillMaxSize()) {

            Text(
                note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                if (note.content.isBlank()) "(Empty note)"
                else note.content.take(50) + if (note.content.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(note.createdAt)),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(timeLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
