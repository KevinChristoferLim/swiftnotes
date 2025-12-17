package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.unit.IntOffset
import com.example.wifty.model.Folder
import com.example.wifty.viewmodel.FolderViewModel
import com.example.wifty.viewmodel.NotesViewModel
import com.example.wifty.ui.screens.modules.TopNavBarWithBack
import com.example.wifty.ui.screens.login.AuthViewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    viewModel: FolderViewModel,
    notesVM: NotesViewModel,
    authViewModel: AuthViewModel,
    onCreateFolder: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onBack: () -> Unit,
    onCreateNote: (String) -> Unit
) {
    val folders by viewModel.folders.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    // --- Search State ---
    var searchQuery by remember { mutableStateOf("") }

    // --- Popup State ---
    var popupVisible by remember { mutableStateOf(false) }
    var popupOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }

    // --- Compute filtered lists directly in composable ---
    val filteredFolders = if (searchQuery.isNotBlank()) {
        folders.filter { it.title.contains(searchQuery, ignoreCase = true) }
    } else folders

    LaunchedEffect(authState.token, authState.user) {
        authState.token?.let {
            viewModel.setCurrentUser(authState.user?.id, it)
            viewModel.refreshFolders()
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopNavBarWithBack(
                title = "All Folders",
                subtitle = "Subfolders",
                onBack = onBack,
                showProfile = true,
                onOpenProfile = { /* open profile */ },
                onSearchClick = { query, type ->
                    searchQuery = query
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFolder,
                containerColor = Color(0xFF4B63FF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Folder", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredFolders, key = { it.id }) { folder ->
                    val noteCount = filteredNotes.count { it.folderId?.toString() == folder.id }

                    FolderCard(
                        name = folder.title,
                        hashtag = folder.tag,
                        notesCount = folder.noteIds.size,
                        gradientColors = listOf(
                            Color(folder.colorLong.toInt()),
                            Color(folder.colorLong.toInt()).copy(alpha = 0.7f)
                        ),
                        onClick = { onOpenFolder(folder.id) },
                        onLongPress = { pressOffset ->
                            selectedFolder = folder
                            popupOffset = pressOffset
                            popupVisible = true
                        }
                    )
                }
            }
        }
    }

    // ------------------------------
    // Popup Menu
    // ------------------------------
    if (popupVisible && selectedFolder != null) {
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(popupOffset.x.toInt(), popupOffset.y.toInt()),
            onDismissRequest = { popupVisible = false }
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    Modifier
                        .width(160.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        "Rename",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                popupVisible = false
                                showRenameDialog = true
                            }
                            .padding(12.dp)
                    )
                    Text(
                        "Add description",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                popupVisible = false
                                showDescriptionDialog = true
                            }
                            .padding(12.dp)
                    )
                    Text(
                        "Add note",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                popupVisible = false
                                selectedFolder?.let { folder ->
                                    onCreateNote(folder.id)
                                }
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }

    // ------------------------------
    // Rename Dialog
    // ------------------------------
    if (showRenameDialog && selectedFolder != null) {
        RenameFolderDialog(
            initialName = selectedFolder?.title ?: "",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                selectedFolder?.let { viewModel.renameFolder(it.id, newName) }
                showRenameDialog = false
            }
        )
    }

    // ------------------------------
    // Description Dialog
    // ------------------------------
    if (showDescriptionDialog && selectedFolder != null) {
        DescriptionFolderDialog(
            initialDescription = selectedFolder?.description ?: "",
            onDismiss = { showDescriptionDialog = false },
            onConfirm = { newDesc ->
                selectedFolder?.let { viewModel.updateFolderDescription(it.id, newDesc) }
                showDescriptionDialog = false
            }
        )
    }
}

// ------------------------------
// FolderCard
// ------------------------------
@Composable
fun FolderCard(
    name: String,
    hashtag: String?,
    notesCount: Int,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    onLongPress: (Offset) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { offset -> onLongPress(offset) }
                )
            }
            .background(
                brush = Brush.verticalGradient(gradientColors),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            if(hashtag != null) {
                Text(hashtag, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        Text(
            text = "$notesCount notes",
            modifier = Modifier.align(Alignment.BottomEnd),
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

// ------------------------------
// Dialogs
// ------------------------------
@Composable
fun RenameFolderDialog(initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rename Folder", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    placeholder = { Text("New name") }
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(text) }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun DescriptionFolderDialog(initialDescription: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialDescription) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Folder Description", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    placeholder = { Text("Description") }
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(text) }) { Text("Save") }
                }
            }
        }
    }
}

// ------------------------------
// Gradient generator
// ------------------------------
fun generateGradient(seed: String): List<Color> {
    val index = (seed.hashCode().absoluteValue % 4)
    return when (index) {
        0 -> listOf(Color(0xFF74EBD5), Color(0xFFACB6E5))
        1 -> listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))
        2 -> listOf(Color(0xFFAAF683), Color(0xFF00CDAC))
        else -> listOf(Color(0xFFFFF6B7), Color(0xFFF6416C))
    }
}
