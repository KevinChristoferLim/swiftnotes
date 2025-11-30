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
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri


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

    val context = LocalContext.current

    // launch the system image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Handle the selected image URI here
            // For example, call a ViewModel function to attach the image
            note?.let { n ->
                viewModel.attachImageToNote(n.id, it)
            }
        }
    }

    //launch the system file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Handle the selected file URI
            note?.let { n ->
                viewModel.attachFileToNote(n.id, it)
            }
        }
    }


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

                    IconButton(onClick = {
                        imagePickerLauncher.launch("image/*")
                    }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Attach Image", tint = Color.White)
                    }


                    IconButton(onClick = {
                        note?.let { n ->
                            // Append a new checklist item
                            val updatedContent = n.content + "\n- [ ] "
                            val updatedNote = n.copy(content = updatedContent)

                            // Update the note in the ViewModel
                            viewModel.updateNote(updatedNote)

                            // Update local state so the UI updates immediately
                            content = TextFieldValue(updatedContent)
                        }
                    }) {
                        Icon(Icons.Filled.List, contentDescription = "Add Checklist", tint = Color.White)
                    }


                    IconButton(onClick = {
                        filePickerLauncher.launch("*/*")  // Allow all types of files
                    }) {
                        Icon(Icons.Filled.AccountBox, contentDescription = "Attach File", tint = Color.White)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(16.dp)) // light gray
                            .padding(24.dp)
                    ) {

                        // TITLE
                        Text(
                            text = "Are you sure you want to delete this note?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // SUBTEXT
                        Text(
                            text = "Deleting this note will permanently remove its contents",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // BUTTONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            // CANCEL BUTTON
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                border = BorderStroke(1.dp, Color.Black)
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // CONFIRM BUTTON
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    note?.let {
                                        viewModel.deleteNote(it.id)
                                        onClose()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7A3FFF), // purple
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
