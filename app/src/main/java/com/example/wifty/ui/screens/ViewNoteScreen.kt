package com.example.wifty.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.wifty.model.Note
import com.example.wifty.ui.screens.login.AuthViewModel
import com.example.wifty.ui.screens.modules.*
import com.example.wifty.ui.screens.modules.formatReminder
import com.example.wifty.viewmodel.NotesViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import java.io.FileNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(
    noteId: String,
    authViewModel: AuthViewModel,
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
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
    var showCollaboratorDialog by remember { mutableStateOf(false) }
    var showUserInfoDialog by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    fun commitAndSave() {
        val token = authState.token ?: return
        val updated = commitBlocksToModelAndSave(token, note, title.text, blocks, colorLong, viewModel)
        if (updated != null) note = updated
    }

    // Handle system back press
    BackHandler {
        commitAndSave()
        onClose()
    }

    fun scheduleAutosaveLocal(scope: CoroutineScope, commitFn: () -> Unit, currentJob: Job?): Job? {
        return scheduleAutosave(scope, currentJob) {
            commitFn()
        }
    }

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

    fun syncFieldValuesFromBlocksLocal(currentBlocks: List<Block>) {
        blockFieldValues = currentBlocks.map { b ->
            when (b) {
                is Block.Text -> TextFieldValue(b.text)
                is Block.Checklist -> TextFieldValue(b.text, selection = TextRange(b.text.length))
                is Block.ImageBlock -> TextFieldValue("")
                is Block.FileBlock -> TextFieldValue("")
            }
        }
    }

    fun insertChecklistAtCursorLocal() {
        if (isLocked) return
        val res = insertChecklistAtCursorPure(blocks, blockFieldValues, focusedBlockIndex, focusedCursorOffset, isLocked)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
    }

    fun deleteChecklistOnBackspaceLocal(index: Int) {
        val res = deleteChecklistOnBackspacePure(blocks, blockFieldValues, index)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
    }

    fun enterInChecklistLocal(index: Int) {
        val res = enterInChecklistPure(blocks, blockFieldValues, index)
        blocks = res.blocks
        blockFieldValues = res.fieldValues
        focusedBlockIndex = res.focusedIndex
        focusedCursorOffset = res.focusedCursorOffset
        scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
    }

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
                scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
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
                scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
            }
        }
    }

    // Observe notes list so we pick up the note if it arrives after navigation (fixes "ghost note" when create -> view races)
    val allNotes by viewModel.notes.collectAsState()

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

    LaunchedEffect(allNotes, noteId) {
        // If the note wasn't available at first (e.g. create -> navigate race), pick it up from the refreshed notes list
        val loaded = allNotes.find { it.id == noteId }
        if (loaded != null && (note == null || note != loaded)) {
            note = loaded
            title = TextFieldValue(loaded.title)
            colorLong = loaded.colorLong
            val parsed = parseContentToBlocks(loaded.content ?: "")
            blocks = parsed
            syncFieldValuesFromBlocksLocal(parsed)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            commitAndSave()
                            onClose()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        
                        // User Profile Icon for Collaboration
                        // Only show if there is more than one participant (owner + collaborators)
                        if ((note?.collaborators?.size ?: 0) > 1) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .clickable { showUserInfoDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                // For simplicity, we use a Person icon if no picture. 
                                // Since we don't have the owner's profile picture URL easily here (it's in collaborators list or owners), 
                                // we show a generic black circle with person icon as requested.
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User Info",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isLocked = !isLocked }) {
                        Icon(Icons.Default.Lock, contentDescription = if (isLocked) "Unlock" else "Lock")
                    }
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(Icons.Default.Star, contentDescription = "Pin", tint = if (isPinned) Color.Yellow else LocalContentColor.current)
                    }
                    IconButton(onClick = { showReminderDialog = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Add reminder")
                    }
                    TextButton(onClick = {
                        commitAndSave()
                        onClose()
                    }) {
                        Text("Done")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showUserInfoDialog) {
            Dialog(onDismissRequest = { showUserInfoDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Collaborators Info",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        note?.collaborators?.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!user.profile_picture.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = user.profile_picture,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(user.username, fontWeight = FontWeight.SemiBold)
                                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showUserInfoDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }

        if (showReminderDialog) {
            ReminderDialog(
                onDismiss = { showReminderDialog = false },
                onSave = { reminder ->
                    authState.token?.let { token ->
                        viewModel.addReminderToNote(token, noteId, reminder)
                    }
                    showReminderDialog = false
                }
            )
        }
        if (showCollaboratorDialog) {
            CollaboratorDialog(
                onDismiss = { showCollaboratorDialog = false },
                onConfirm = { email ->
                    authState.token?.let { token ->
                        viewModel.addCollaboratorToNote(token, noteId, email)
                    }
                    showCollaboratorDialog = false
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

                BasicTextField(
                    value = title,
                    onValueChange = {
                        if (!isLocked) {
                            title = it
                            scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
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
                note?.reminder?.let { reminder ->
                    Spacer(Modifier.height(8.dp))
                    Text(formatReminder(reminder) ?: "Reminder set", fontSize = 13.sp, color = Color.Gray)
                }

                // Collaborators Section
                // Only show if there is more than one participant (owner + collaborators)
                note?.collaborators?.let { collaborators ->
                    if (collaborators.size > 1) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Collaborators",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        collaborators.forEach { collaborator ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!collaborator.profile_picture.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = collaborator.profile_picture,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "User Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = collaborator.username,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = collaborator.email,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        authState.token?.let { token ->
                                            viewModel.removeCollaborator(token, noteId, collaborator.id)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove collaborator",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

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
                                        scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
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
                                        scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
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
                                        scheduleAutosaveLocal(coroutineScope, ::commitAndSave, autosaveJob)?.also { autosaveJob = it }
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
                            var expanded by remember { mutableStateOf(false) }

                            // Inline image - scaled to width and keeps a minimum height
                            AsyncImage(
                                model = Uri.parse(block.uri),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp)
                                    .padding(vertical = 8.dp)
                                    .clickable { expanded = true },
                                contentScale = ContentScale.FillWidth
                            )

                            // Full-screen viewer when user taps the image
                            if (expanded) {
                                Dialog(onDismissRequest = { expanded = false }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                            .clickable { expanded = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = Uri.parse(block.uri),
                                            contentDescription = "Image (full)",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }

                        is Block.FileBlock -> {
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
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse(block.uri)
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
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
                        authState.token?.let { token ->
                            note?.let { viewModel.copyNote(token, it) }
                        }
                        showMoreMenu = false
                    },
                    onCollaboratorClick = {
                        showCollaboratorDialog = true
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
                        authState.token?.let { token ->
                            note?.let {
                                viewModel.deleteNote(token, it.id)
                                onClose()
                            }
                        }
                        showDeleteDialog = false
                    },
                    onCancel = { showDeleteDialog = false }
                )
            }
        }
    }
}

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
    media: Block
): InsertResult {
    val newBlocks = blocks.toMutableList()
    val newFields = fieldValues.toMutableList()

    if (focusedIndex < 0) {
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

        newBlocks[focusedIndex] = Block.Text(left)
        newFields[focusedIndex] = TextFieldValue(left)

        val insertPos = focusedIndex + 1
        newBlocks.add(insertPos, media)
        newFields.add(insertPos, TextFieldValue(""))

        if (right.isNotEmpty()) {
            newBlocks.add(insertPos + 1, Block.Text(right))
            newFields.add(insertPos + 1, TextFieldValue(right))
            return InsertResult(newBlocks, newFields, insertPos + 1, 0)
        } else {
            return InsertResult(newBlocks, newFields, insertPos + 1, 0)
        }
    } else {
        val insertPos = (focusedIndex + 1).coerceAtMost(newBlocks.size)
        newBlocks.add(insertPos, media)
        newFields.add(insertPos, TextFieldValue(""))
        return InsertResult(newBlocks, newFields, insertPos, 0)
    }
}
