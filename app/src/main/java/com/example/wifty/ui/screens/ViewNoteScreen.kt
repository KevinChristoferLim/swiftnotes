package com.example.wifty.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifty.model.Note
import com.example.wifty.viewmodel.NotesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.Job
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.wifty.ui.screens.modules.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(
    noteId: String,
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
    // UI state (kept local in the screen)
    var note by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var colorLong by remember { mutableStateOf(0xFF4B63FFu.toLong()) }

    var blocks by remember { mutableStateOf<List<Block>>(listOf(Block.Text(""))) }
    var blockFieldValues by remember { mutableStateOf<List<TextFieldValue>>(listOf(TextFieldValue(""))) }

    var focusedBlockIndex by remember { mutableStateOf(0) }
    var focusedCursorOffset by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    var autosaveJob by remember { mutableStateOf<Job?>(null) }

    var isLocked by rememberSaveable { mutableStateOf(false) }
    var isPinned by rememberSaveable { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    // image / file pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { if (!isLocked) note?.let { n -> viewModel.attachImageToNote(n.id, it) } }
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { if (!isLocked) note?.let { n -> viewModel.attachFileToNote(n.id, it) } }
    }

    //Helper lambdas that use the pure functions in other modules

    fun syncFieldValuesFromBlocksLocal(currentBlocks: List<Block>) {
        blockFieldValues = currentBlocks.map { b ->
            when (b) {
                is Block.Text -> TextFieldValue(b.text)
                is Block.Checklist -> TextFieldValue(b.text, selection = TextRange(b.text.length))
            }
        }
    }

    fun scheduleAutosaveLocal() {
        autosaveJob = scheduleAutosave(coroutineScope, autosaveJob) {
            // run commit on coroutine context
            val updated = commitBlocksToModelAndSave(note, title.text, blocks, colorLong, viewModel)
            if (updated != null) note = updated
        }
    }

    // insert checklist (wraps pure function)
    fun insertChecklistAtCursorLocal() {
        if (isLocked) return
        val res = insertChecklistAtCursorPure(blocks, blockFieldValues, focusedBlockIndex, focusedCursorOffset, isLocked)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal()
    }

    // delete checklist on backspace (used by key handling)
    fun deleteChecklistOnBackspaceLocal(index: Int) {
        val res = deleteChecklistOnBackspacePure(blocks, blockFieldValues, index)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal()
    }

    // enter in checklist
    fun enterInChecklistLocal(index: Int) {
        val res = enterInChecklistPure(blocks, blockFieldValues, index)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal()
    }

    // --- Load note by id (once) ---
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId) { loaded ->
            note = loaded
            loaded?.let {
                title = TextFieldValue(it.title)
                colorLong = it.colorLong
                val parsed = parseContentToBlocks(it.content ?: "")
                blocks = parsed
                syncFieldValuesFromBlocksLocal(parsed)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isLocked = !isLocked }) {
                        Icon(Icons.Default.Lock, contentDescription = if (isLocked) "Unlock" else "Lock")
                    }
                    IconButton(onClick = {
                        isPinned = !isPinned
                    }) {
                        Icon(Icons.Default.Star, contentDescription = "Pin", tint = if (isPinned) Color.Yellow else LocalContentColor.current)
                    }
                    IconButton(onClick = { showReminderDialog = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Add reminder")
                    }
                    TextButton(onClick = {
                        // immediate save & close
                        val updated = commitBlocksToModelAndSave(note, title.text, blocks, colorLong, viewModel)
                        if (updated != null) note = updated
                        onClose()
                    }) {
                        Text("Done")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showReminderDialog) {
            ReminderDialog(
                onDismiss = { showReminderDialog = false },
                onSave = { reminder ->
                    // TODO: attach reminder to note via viewModel if needed
                    showReminderDialog = false
                }
            )
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF7F5FF), Color.White)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 120.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // Title editor
                BasicTextField(
                    value = title,
                    onValueChange = {
                        if (!isLocked) {
                            title = it
                            scheduleAutosaveLocal()
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (title.text.isEmpty()) Text(
                            "Title",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        inner()
                    }
                )

                Spacer(Modifier.height(8.dp))
                Text("Last updated", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))

                // Render each block using the small composables
                for ((index, block) in blocks.withIndex()) {
                    when (block) {
                        is Block.Text -> {
                            val tfv = blockFieldValues.getOrNull(index) ?: TextFieldValue(block.text)
                            TextBlockEditor(
                                value = tfv,
                                isLocked = isLocked,
                                onValueChange = { newTfV ->
                                    if (!isLocked) {
                                        val updatedVals = blockFieldValues.toMutableList()
                                        if (index < updatedVals.size) updatedVals[index] = newTfV else updatedVals.add(newTfV)
                                        blockFieldValues = updatedVals

                                        val newBlocks = blocks.toMutableList()
                                        newBlocks[index] = Block.Text(newTfV.text)
                                        blocks = newBlocks

                                        focusedBlockIndex = index
                                        focusedCursorOffset = newTfV.selection.start
                                        scheduleAutosaveLocal()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is Block.Checklist -> {
                            val tfv = blockFieldValues.getOrNull(index) ?: TextFieldValue(block.text)
                            ChecklistBlockEditor(
                                value = tfv,
                                checked = block.checked,
                                isLocked = isLocked,
                                onCheckedChange = { checked ->
                                    if (!isLocked) {
                                        val newBlocks = blocks.toMutableList()
                                        newBlocks[index] = Block.Checklist(block.text, checked)
                                        blocks = newBlocks
                                        blockFieldValues = syncFieldValuesFromBlocksPure(newBlocks)
                                        scheduleAutosaveLocal()
                                    }
                                },
                                onValueChange = { newTfV ->
                                    if (!isLocked) {
                                        val updatedVals = blockFieldValues.toMutableList()
                                        if (index < updatedVals.size) updatedVals[index] = newTfV else updatedVals.add(newTfV)
                                        blockFieldValues = updatedVals

                                        val newBlocks = blocks.toMutableList()
                                        newBlocks[index] = Block.Checklist(newTfV.text, block.checked)
                                        blocks = newBlocks

                                        focusedBlockIndex = index
                                        focusedCursorOffset = newTfV.selection.start
                                        scheduleAutosaveLocal()
                                    }
                                },
                                onKeyEvent = { ke ->
                                    if (isLocked) return@ChecklistBlockEditor false

                                    val isBackspace = ke.key == Key.Backspace
                                    val isEnter = ke.key == Key.Enter
                                    val isKeyUp = ke.type == KeyEventType.KeyUp

                                    if (isBackspace && isKeyUp && tfv.text.isEmpty()) {
                                        deleteChecklistOnBackspaceLocal(index)
                                        true
                                    } else if (isEnter && isKeyUp) {
                                        enterInChecklistLocal(index)
                                        true
                                    } else false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }

            // Bottom toolbar & more-menu
            var showMoreMenu by remember { mutableStateOf(false) }
            var showDeleteDialog by remember { mutableStateOf(false) }

            NoteBottomBar(
                onImageClick = { if (!isLocked) imagePickerLauncher.launch("image/*") },
                onInsertChecklistClick = { insertChecklistAtCursorLocal() },
                onFileClick = { if (!isLocked) filePickerLauncher.launch("*/*") },
                onMoreClick = { showMoreMenu = !showMoreMenu },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (showMoreMenu) {
                FloatingMoreMenu(
                    onDeleteClick = {
                        showDeleteDialog = true
                        showMoreMenu = false
                    },
                    onCopyClick = {
                        note?.let { viewModel.copyNote(it) }
                        showMoreMenu = false
                    },
                    onCollaboratorClick = {
                        showMoreMenu = false
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-20).dp, y = (-130).dp)
                )
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        note?.let {
                            viewModel.deleteNote(it.id)
                            onClose()
                        }
                        showDeleteDialog = false
                    },
                    onCancel = { showDeleteDialog = false }
                )
            }
        }
    }
}
