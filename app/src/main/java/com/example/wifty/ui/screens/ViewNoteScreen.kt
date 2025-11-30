package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import com.example.wifty.model.Note
import com.example.wifty.viewmodel.NotesViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(
    noteId: String,
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
    // Local states
    var note by remember { mutableStateOf<Note?>(null) }

    var title by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var content by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var colorLong by remember { mutableStateOf(0xFF4B63FFu.toLong()) }

    // --- Load Note ---
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId) { loaded ->
            note = loaded
            loaded?.let {
                title = TextFieldValue(it.title)
                content = TextFieldValue(it.content)
                colorLong = it.colorLong
            }
        }
    }

    // --- Scaffold ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            note?.let {
                                val updated = it.copy(
                                    title = title.text,
                                    content = content.text,
                                    colorLong = colorLong
                                )
                                viewModel.updateNote(updated)
                            }
                            onClose()
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF7F5FF), Color.White)
                    )
                )
                .padding(horizontal = 16.dp)
        ) {

            // ==== MAIN COLUMN ====
            Column(modifier = Modifier.fillMaxSize()) {

                Spacer(Modifier.height(8.dp))

                // --- TITLE ---
                BasicTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        note?.let { n ->
                            viewModel.updateNote(
                                n.copy(
                                    title = it.text,
                                    content = content.text,
                                    colorLong = colorLong
                                )
                            )
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    decorationBox = { inner ->
                        if (title.text.isEmpty()) {
                            Text(
                                "Title",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        inner()
                    }
                )

                Spacer(Modifier.height(6.dp))

                // --- STATIC DATE ---
                Text(
                    "Last updated",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // --- CONTENT ---
                BasicTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        note?.let { n ->
                            viewModel.updateNote(
                                n.copy(
                                    title = title.text,
                                    content = it.text,
                                    colorLong = colorLong
                                )
                            )
                        }
                    },
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    decorationBox = { inner ->
                        if (content.text.isEmpty()) {
                            Text(
                                "Write something...",
                                color = Color.Gray,
                                fontSize = 18.sp
                            )
                        }
                        inner()
                    }
                )
            }

            // *****************************************************************
            // ------------------ BOTTOM ACTION BAR + MORE MENU ----------------
            // *****************************************************************

            var showMoreMenu by remember { mutableStateOf(false) }
            var showDeleteDialog by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {

                // --- ACTION BAR ---
                Row(
                    modifier = Modifier
                        .background(Color(0xFFB9A8E6), RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(onClick = { /* camera later */ }) {
                        Icon(Icons.Outlined.Info , contentDescription = null, tint = Color.White)
                    }

                    IconButton(onClick = { /* checklist */ }) {
                        Icon(Icons.Filled.List, contentDescription = null, tint = Color.White)
                    }

                    IconButton(onClick = { /* attachment */ }) {
                        Icon(Icons.Filled.AccountBox, contentDescription = null, tint = Color.White)
                    }

                    IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color.White)
                    }
                }

                // --- EXPANDED MENU ---
                if (showMoreMenu) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, bottom = 70.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFEAE2FF), Color(0xFFF7F1FF))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {

                        // DELETE
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDeleteDialog = true
                                    showMoreMenu = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete")
                        }

                        // COPY
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    note?.let { viewModel.copyNote(it) }
                                    showMoreMenu = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copy")
                        }


                        // COLLABORATOR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // implement later
                                    showMoreMenu = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Collaborator")
                        }
                    }
                }
            }

            // ---------- DELETE CONFIRMATION ----------
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete this note?") },
                    text = { Text("This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            note?.let {
                                viewModel.deleteNote(it.id)
                                onClose()
                            }
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
