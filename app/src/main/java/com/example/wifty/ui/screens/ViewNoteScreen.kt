package com.example.wifty.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wifty.model.Note
import com.example.wifty.ui.screens.modules.*
import com.example.wifty.viewmodel.NotesViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import java.io.FileNotFoundException
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

    val context = LocalContext.current

    // ---- Helpers (must exist before being referenced) ----

    fun commitAndAssign() {
        val updated = commitBlocksToModelAndSave(note, title.text, blocks, colorLong, viewModel)
        if (updated != null) note = updated
    }

    fun scheduleAutosaveLocal(scope: CoroutineScope, commitFn: () -> Unit, currentJob: Job?): Job? {
        // return the Job created by scheduleAutosave so callers can reassign autosaveJob
        return scheduleAutosave(scope, currentJob) {
            commitFn()
        }
    }

    // Helper to query display name & size from content URI (returns Pair<name?, size?>)
    fun queryFilenameAndSize(context: android.content.Context, uri: Uri): Pair<String?, Long?> {
        return try {
            var name: String? = null
            var size: Long? = null
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex)
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
            Pair(name, size)
        } catch (e: SecurityException) {
            Pair(null, null)
        } catch (e: FileNotFoundException) {
            Pair(null, null)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    //Helper lambdas that use the pure functions in other modules

    fun syncFieldValuesFromBlocksLocal(currentBlocks: List<Block>) {
        blockFieldValues = currentBlocks.map { b ->
            when (b) {
                is Block.Text -> TextFieldValue(b.text)
                is Block.Checklist -> TextFieldValue(b.text, selection = TextRange(b.text.length))
                is Block.ImageBlock -> TextFieldValue("") // images don't need editor value
                is Block.FileBlock -> TextFieldValue("")  // files don't need editor value
            }
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
        scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
    }

    // delete checklist on backspace (used by key handling)
    fun deleteChecklistOnBackspaceLocal(index: Int) {
        val res = deleteChecklistOnBackspacePure(blocks, blockFieldValues, index)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
    }

    // enter in checklist
    fun enterInChecklistLocal(index: Int) {
        val res = enterInChecklistPure(blocks, blockFieldValues, index)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
    }

    // --- Pickers (insert media at cursor) ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (!isLocked) {
                val res = insertMediaAtCursor(blocks, blockFieldValues, focusedBlockIndex, focusedCursorOffset, Block.ImageBlock(it.toString()))
                blocks = res.blocks
                blockFieldValues = res.fieldValues
                focusedBlockIndex = res.focusedIndex
                focusedCursorOffset = res.focusedCursorOffset
                scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (!isLocked) {
                val info = queryFilenameAndSize(context, it)
                val fname = info.first ?: "file"
                val fsize = info.second ?: 0L
                val fileBlock = Block.FileBlock(it.toString(), fname, fsize)

                val res = insertMediaAtCursor(blocks, blockFieldValues, focusedBlockIndex, focusedCursorOffset, fileBlock)
                blocks = res.blocks
                blockFieldValues = res.fieldValues
                focusedBlockIndex = res.focusedIndex
                focusedCursorOffset = res.focusedCursorOffset
                scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
            }
        }
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

    // ---------- UI scaffold ----------
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
                            scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
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
                                        scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
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
                                        scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
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
                                        scheduleAutosaveLocal(coroutineScope, ::commitAndAssign, autosaveJob)?.also { autosaveJob = it }
                                    }
                                },
                                onKeyEvent = { ke ->
                                    if (isLocked) return@ChecklistBlockEditor false

                                    val androidEvent = ke.nativeKeyEvent
                                    val isBackspace = androidEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL
                                    val isEnter = androidEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER
                                    val isKeyUp = androidEvent.action == android.view.KeyEvent.ACTION_UP

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

                        is Block.ImageBlock -> {
                            // show the image inline
                            AsyncImage(
                                model = Uri.parse(block.uri),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp)
                                    .padding(vertical = 8.dp)
                            )
                        }

                        is Block.FileBlock -> {
                            // file card UI
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(block.filename, style = MaterialTheme.typography.bodyLarge)
                                        val sizeText = remember(block.sizeBytes) {
                                            if (block.sizeBytes <= 0) "" else {
                                                val kb = block.sizeBytes / 1024.0
                                                when {
                                                    kb < 1024 -> String.format("%.1f KB", kb)
                                                    else -> String.format("%.1f MB", kb / 1024.0)
                                                }
                                            }
                                        }
                                        if (sizeText.isNotEmpty()) Text(sizeText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    IconButton(onClick = {
                                        // open the file using Intent
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse(block.uri)
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // ignore or show snackbar in future
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Open")
                                    }
                                }
                            }
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

/**
 * Insert a media block (image/file) at the focused block+cursor.
 * Returns new blocks, new field values, new focus index and cursor offset.
 *
 * Simple behavior:
 * - If focused block is Text, split the text at cursor into leftText and rightText,
 *   replace current block with leftText, insert media block, then insert rightText as Text block (if non-empty).
 * - If focused block is Checklist or other, insert media after current block.
 */
data class InsertResult(
    val blocks: List<Block>,
    val fieldValues: List<TextFieldValue>,
    val focusedIndex: Int,
    val focusedCursorOffset: Int
)

fun insertMediaAtCursor(
    blocks: List<Block>,
    fieldValues: List<TextFieldValue>,
    focusedIndex: Int,
    cursorOffset: Int,
    media: Block // Block.ImageBlock or Block.FileBlock
): InsertResult {
    val newBlocks = blocks.toMutableList()
    val newFields = fieldValues.toMutableList()

    if (focusedIndex < 0) {
        // append at end
        newBlocks.add(media)
        newFields.add(TextFieldValue(""))
        return InsertResult(newBlocks, newFields, newBlocks.indexOf(media), 0)
    }

    val current = newBlocks.getOrNull(focusedIndex)
    if (current is Block.Text) {
        val tfv = newFields.getOrNull(focusedIndex) ?: TextFieldValue(current.text)
        val text = tfv.text
        val left = if (cursorOffset <= text.length) text.substring(0, cursorOffset) else text
        val right = if (cursorOffset <= text.length) text.substring(cursorOffset) else ""

        // replace current text block with left, insert media, then right (if any)
        newBlocks[focusedIndex] = Block.Text(left)
        newFields[focusedIndex] = TextFieldValue(left)

        val insertPos = focusedIndex + 1
        newBlocks.add(insertPos, media)
        newFields.add(insertPos, TextFieldValue(""))

        if (right.isNotEmpty()) {
            newBlocks.add(insertPos + 1, Block.Text(right))
            newFields.add(insertPos + 1, TextFieldValue(right))
            return InsertResult(newBlocks, newFields, insertPos + 1, 0) // focus on text after media
        } else {
            return InsertResult(newBlocks, newFields, insertPos + 1, 0)
        }
    } else {
        // not a text block: insert after current block
        val insertPos = (focusedIndex + 1).coerceAtMost(newBlocks.size)
        newBlocks.add(insertPos, media)
        newFields.add(insertPos, TextFieldValue(""))
        return InsertResult(newBlocks, newFields, insertPos, 0)
    }
}
