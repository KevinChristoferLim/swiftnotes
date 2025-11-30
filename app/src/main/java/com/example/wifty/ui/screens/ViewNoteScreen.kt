package com.example.wifty.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifty.model.Note
import com.example.wifty.model.ChecklistItem
import com.example.wifty.viewmodel.NotesViewModel
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(
    noteId: String,
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
    var note by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var checklist by remember { mutableStateOf(listOf<ChecklistItem>()) }
    var colorLong by remember { mutableStateOf(0xFF4B63FFu.toLong()) }

    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { note?.let { n -> viewModel.attachImageToNote(n.id, it) } }
    }

    // File picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { note?.let { n -> viewModel.attachFileToNote(n.id, it) } }
    }

    // Load note
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId) { loaded ->
            note = loaded
            loaded?.let {
                title = TextFieldValue(it.title)
                content = TextFieldValue(it.content)
                colorLong = it.colorLong
                checklist = it.checklist
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            note?.let {
                                val updated = it.copy(
                                    title = title.text,
                                    content = content.text,
                                    colorLong = colorLong,
                                    checklist = checklist
                                )
                                viewModel.updateNote(updated)
                            }
                            onClose()
                        }
                    ) { Text("Done") }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF7F5FF), Color.White)))
                .padding(horizontal = 16.dp)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                Spacer(Modifier.height(8.dp))

                // Title
                BasicTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        note?.let { n ->
                            viewModel.updateNote(
                                n.copy(title = it.text, content = content.text, colorLong = colorLong, checklist = checklist)
                            )
                        }
                    },
                    textStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    decorationBox = { inner ->
                        if (title.text.isEmpty()) Text("Title", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        inner()
                    }
                )

                Spacer(Modifier.height(6.dp))

                // Last updated text
                Text("Last updated", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))

                // Content field
                BasicTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        note?.let { n ->
                            viewModel.updateNote(
                                n.copy(title = title.text, content = it.text, colorLong = colorLong, checklist = checklist)
                            )
                        }
                    },
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    decorationBox = { inner ->
                        if (content.text.isEmpty()) Text("Write something...", color = Color.Gray, fontSize = 18.sp)
                        inner()
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Render checklist
                checklist.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { checked ->
                                checklist = checklist.toMutableList().also { it[index] = it[index].copy(isChecked = checked) }
                                note?.let { n -> viewModel.updateNote(n.copy(checklist = checklist)) }
                            }
                        )
                        BasicTextField(
                            value = TextFieldValue(item.text),
                            onValueChange = { newText ->
                                checklist = checklist.toMutableList().also {
                                    it[index] = it[index].copy(text = newText.text)
                                }
                                note?.let { n -> viewModel.updateNote(n.copy(checklist = checklist)) }
                            },
                            textStyle = TextStyle(fontSize = 18.sp),
                            modifier = Modifier.weight(1f)
                        )

                    }
                }

                Spacer(Modifier.height(80.dp))
            }

            // Bottom action bar
            var showMoreMenu by remember { mutableStateOf(false) }
            var showDeleteDialog by remember { mutableStateOf(false) }

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier.background(Color(0xFFB9A8E6), RoundedCornerShape(12.dp)).padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Image picker
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Attach Image", tint = Color.White)
                    }

                    // Add checklist item
                    IconButton(onClick = { checklist = checklist + ChecklistItem(text = "") }) {
                        Icon(Icons.Filled.List, contentDescription = "Add Checklist", tint = Color.White)
                    }

                    // File picker
                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(Icons.Filled.AccountBox, contentDescription = "Attach File", tint = Color.White)
                    }

                    // More menu
                    IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color.White)
                    }
                }

                // Expanded menu
                if (showMoreMenu) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, bottom = 70.dp)
                            .background(brush = Brush.verticalGradient(listOf(Color(0xFFEAE2FF), Color(0xFFF7F1FF))), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                showDeleteDialog = true
                                showMoreMenu = false
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                note?.let { viewModel.copyNote(it) }
                                showMoreMenu = false
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copy")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showMoreMenu = false }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Collaborator")
                        }
                    }
                }
            }

            // Delete confirmation
            if (showDeleteDialog) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF2F2F2), shape = RoundedCornerShape(16.dp)).padding(24.dp)
                    ) {
                        Text("Are you sure you want to delete this note?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(Modifier.height(8.dp))
                        Text("Deleting this note will permanently remove its contents", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
                                border = BorderStroke(1.dp, Color.Black)
                            ) { Text("Cancel") }

                            Spacer(Modifier.width(12.dp))

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    note?.let {
                                        viewModel.deleteNote(it.id)
                                        onClose()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3FFF), contentColor = Color.White)
                            ) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
