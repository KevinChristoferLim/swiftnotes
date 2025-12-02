package com.example.wifty.ui.screens.notes

import androidx.compose.foundation.Image // Import Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // Import ContentScale
import androidx.compose.ui.res.painterResource // Import painterResource
import androidx.compose.ui.unit.dp
import com.example.wifty.R // Import your Resource file
import com.example.wifty.model.Note
import com.example.wifty.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Helper function for time
fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)} minutes ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)} hours ago"
        diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onCreateNewNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenFolders: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()

    // 1. If Empty -> Show Landing Screen
    if (notes.isEmpty()) {
        LandingScreen(
            onCreateNewNote = onCreateNewNote,
            onOpenFolders = onOpenFolders,
            onOpenProfile = onOpenProfile
        )
    } else {
        // 2. If Not Empty -> Show Grid with Center FAB
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateNewNote,
                    containerColor = Color(0xFF4B63FF),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(26.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                        Spacer(Modifier.width(18.dp))
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Folders",
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onOpenFolders() }
                        )
                        Spacer(Modifier.width(18.dp))

                        // ✨ UPDATED: PROFILE IMAGE ✨
                        Image(
                            painter = painterResource(id = R.drawable.sample_profile),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop, // Ensures image fills the circle
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape) // Makes it round
                                .clickable { onOpenProfile() }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Notes Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notes) { note ->
                        NoteCard(note = note, onClick = { onOpenNote(note.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    val timeLabel = remember(note.updatedAt) { formatRelativeTime(note.updatedAt) }
    val backgroundColor = if (note.colorLong == 0L) Color(0xFFD3E3FD) else Color(note.colorLong)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(backgroundColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(note.createdAt)),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(timeLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}