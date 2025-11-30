package com.example.wifty.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.*
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
import com.example.wifty.model.Note
import com.example.wifty.model.ChecklistItem
import com.example.wifty.viewmodel.NotesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.input.key.*
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import java.util.*

// Marker format: [[CHECKLIST:BASE64_TEXT:0_or_1]]
private val CHECKLIST_REGEX = Pattern.compile("\\[\\[CHECKLIST:([A-Za-z0-9+/=]+):(0|1)]]")

private sealed class Block {
    data class Text(var text: String) : Block()
    data class Checklist(var text: String, var checked: Boolean) : Block()
}

data class ReminderData(
    val dateMillis: Long? = null,           // date portion (midnight local) or null
    val timeMillis: Long? = null,           // time portion in millis (time-of-day) or absolute timestamp (we store as absolute)
    val repeat: List<String> = emptyList(), // e.g. ["Everyday"] or ["Monday","Wednesday"]
    val location: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(
    noteId: String,
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
    var note by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf(TextFieldValue("")) }
    // content string is stored in the model; we will reconstruct it from blocks whenever blocks change
    var colorLong by remember { mutableStateOf(0xFF4B63FFu.toLong()) }

    // Blocks (text + checklist) is the UI source of truth
    var blocks by remember { mutableStateOf<List<Block>>(listOf(Block.Text(""))) }

    // Per-block TextFieldValue states (keeps selection/cursor)
    var blockFieldValues by remember { mutableStateOf<List<TextFieldValue>>(listOf(TextFieldValue(""))) }

    // Track which block is focused, and cursor offset (for insertion)
    var focusedBlockIndex by remember { mutableStateOf(0) }
    var focusedCursorOffset by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    // Coroutine scope + autosave job for debounce
    val coroutineScope = rememberCoroutineScope()
    var autosaveJob by remember { mutableStateOf<Job?>(null) }

    // New states: lock, pin, reminder dialog
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var isPinned by rememberSaveable { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (!isLocked) note?.let { n -> viewModel.attachImageToNote(n.id, it) }
        }
    }

    // File picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (!isLocked) note?.let { n -> viewModel.attachFileToNote(n.id, it) }
        }
    }

    // Helpers: Marker encoding
    fun encodeForMarker(s: String): String {
        val bytes = s.toByteArray(StandardCharsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeMarkerValue(b64: String): String {
        return try {
            val bytes = Base64.decode(b64, Base64.NO_WRAP)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    // Parse content -> blocks
    fun parseContentToBlocks(content: String): List<Block> {
        val matcher = CHECKLIST_REGEX.matcher(content)
        val result = mutableListOf<Block>()
        var lastEnd = 0
        while (matcher.find()) {
            val start = matcher.start()
            if (start > lastEnd) {
                val textSegment = content.substring(lastEnd, start)
                // preserve segment even if empty
                result.add(Block.Text(textSegment))
            }
            val b64 = matcher.group(1) ?: ""
            val checked = matcher.group(2) == "1"
            val checklistText = decodeMarkerValue(b64)
            result.add(Block.Checklist(checklistText, checked))
            lastEnd = matcher.end()
        }
        if (lastEnd < content.length) {
            result.add(Block.Text(content.substring(lastEnd)))
        }
        if (result.isEmpty()) result.add(Block.Text(""))
        return result
    }

    // Serialize blocks -> content
    fun serializeBlocksToContent(blocks: List<Block>): String {
        val sb = StringBuilder()
        for (b in blocks) {
            when (b) {
                is Block.Text -> sb.append(b.text)
                is Block.Checklist -> {
                    val enc = encodeForMarker(b.text)
                    val flag = if (b.checked) "1" else "0"
                    sb.append("[[CHECKLIST:").append(enc).append(":").append(flag).append("]]")
                }
            }
        }
        return sb.toString()
    }

    // Normalize blocks BEFORE saving:
    // ensure no Text block contains checklist markers; split them into proper blocks
    fun normalizeBlocksForSave(inputBlocks: List<Block>): List<Block> {
        val out = mutableListOf<Block>()
        for (b in inputBlocks) {
            if (b is Block.Text) {
                val text = b.text
                val matcher = CHECKLIST_REGEX.matcher(text)
                var lastEnd = 0
                var foundAny = false
                while (matcher.find()) {
                    foundAny = true
                    val start = matcher.start()
                    if (start > lastEnd) {
                        out.add(Block.Text(text.substring(lastEnd, start)))
                    }
                    val b64 = matcher.group(1) ?: ""
                    val checked = matcher.group(2) == "1"
                    val checklistText = decodeMarkerValue(b64)
                    out.add(Block.Checklist(checklistText, checked))
                    lastEnd = matcher.end()
                }
                if (!foundAny) {
                    // no markers inside this text -> keep as-is
                    out.add(Block.Text(text))
                } else {
                    if (lastEnd < text.length) {
                        out.add(Block.Text(text.substring(lastEnd)))
                    }
                }
            } else {
                out.add(b)
            }
        }

        // Ensure we never return an empty list
        if (out.isEmpty()) out.add(Block.Text(""))

        return out
    }

    // Sync blockFieldValues length with blocks
    fun syncFieldValuesFromBlocks(currentBlocks: List<Block>) {
        val newFieldVals = currentBlocks.map { b ->
            when (b) {
                is Block.Text -> TextFieldValue(b.text)
                is Block.Checklist -> TextFieldValue(b.text, selection = TextRange(b.text.length))
            }
        }
        blockFieldValues = newFieldVals
    }

    // Persist blocks into note.content (normalize first)
    fun commitBlocksToModelAndSave() {
        // Normalize in-memory blocks to ensure no Text contains checklist markers
        val normalized = normalizeBlocksForSave(blocks)
        // Update UI state with normalized blocks so the UI matches what's saved
        blocks = normalized
        syncFieldValuesFromBlocks(normalized)

        val contentString = serializeBlocksToContent(normalized)
        note?.let { n ->
            val updated = n.copy(
                title = title.text,
                content = contentString,
                colorLong = colorLong,
                // keep checklist array in model for compatibility: extract checklist items as a flat list
                checklist = normalized.mapIndexedNotNull { idx, b ->
                    when (b) {
                        is Block.Checklist -> ChecklistItem(
                            text = b.text,
                            isChecked = b.checked,
                            noteId = n.id,
                            order = idx
                        )

                        else -> null
                    }
                }
            )
            viewModel.updateNote(updated)
            note = updated
        }
    }

    // Debounced autosave scheduler (1 second)
    fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = coroutineScope.launch {
            delay(1000L)
            commitBlocksToModelAndSave()
        }
    }

    // Insert a real checklist block at the cursor location inside the focused text block
    // Behavior:
    //  - If current line is empty, replace that current line with a checklist and add an empty text line below
    //  - If current line has text, insert checklist line directly below current line and add an empty text line under it
    fun insertChecklistAtCursor() {
        if (isLocked) return // editing disabled while locked

        // guard
        if (blocks.isEmpty()) {
            blocks = listOf(Block.Checklist("", false), Block.Text(""))
            syncFieldValuesFromBlocks(blocks)
            scheduleAutosave()
            return
        }
        val idx = focusedBlockIndex.coerceIn(0, blocks.lastIndex)
        val focusedBlock = blocks[idx]
        if (focusedBlock is Block.Text) {
            // compute cursor offset inside this block
            val tfv = blockFieldValues.getOrNull(idx) ?: TextFieldValue(focusedBlock.text)
            val cursor = tfv.selection.start.coerceIn(0, tfv.text.length)

            // find current line start/end within this text block
            val text = focusedBlock.text
            val lineStart = text.lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
            val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }

            val currentLine = text.substring(lineStart, lineEnd)

            if (currentLine.isBlank()) {
                // replace current empty line with checklist and keep the rest
                val before = text.substring(0, lineStart)
                val after = text.substring(lineEnd)
                val newBlocks = blocks.toMutableList().also {
                    // replace current text block with (Text(before), Checklist, Text("\n" + after) or Text(after) )
                    it.removeAt(idx)

                    if (before.isNotEmpty()) it.add(
                        idx,
                        Block.Text(before)
                    ) // keep text before line
                    it.add(idx + if (before.isNotEmpty()) 1 else 0, Block.Checklist("", false))
                    // ensure there is an empty text line after checklist
                    it.add(idx + if (before.isNotEmpty()) 2 else 1, Block.Text("\n" + after))
                }
                blocks = newBlocks
            } else {
                // split text block into: beforeLine, currentLine, afterLine -> then insert checklist between currentLine and afterLine
                val before =
                    text.substring(0, lineEnd) // includes current line + trailing newline if any
                val after = text.substring(lineEnd)
                val newBlocks = blocks.toMutableList().also {
                    it.removeAt(idx)
                    // if there's text before (could be empty)
                    if (before.isNotEmpty()) it.add(idx, Block.Text(before))
                    // insert checklist after that
                    it.add(idx + if (before.isNotEmpty()) 1 else 0, Block.Checklist("", false))
                    // add an empty text line after checklist followed by 'after' content
                    it.add(idx + if (before.isNotEmpty()) 2 else 1, Block.Text("\n" + after))
                }
                blocks = newBlocks
            }
            // re-sync field values and pick focus on the newly created checklist block
            syncFieldValuesFromBlocks(blocks)
            // find first checklist after the original index
            val newIndex = (idx + 1).coerceAtMost(blocks.lastIndex)
            focusedBlockIndex = newIndex
            focusedCursorOffset = 0
            scheduleAutosave()
        } else {
            // Focused on checklist -> just insert a new checklist after it
            val newBlocks = blocks.toMutableList()
            newBlocks.add(idx + 1, Block.Checklist("", false))
            // ensure a text block after the inserted checklist for spacing
            newBlocks.add(idx + 2, Block.Text("\n"))
            blocks = newBlocks
            syncFieldValuesFromBlocks(blocks)
            focusedBlockIndex = idx + 1
            focusedCursorOffset = 0
            scheduleAutosave()
        }
    }

    // UI: Load note (parse content -> blocks)
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId) { loaded ->
            note = loaded
            loaded?.let {
                title = TextFieldValue(it.title)
                colorLong = it.colorLong
                val parsed = parseContentToBlocks(it.content ?: "")
                blocks = parsed
                syncFieldValuesFromBlocks(parsed)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Lock toggle
                    IconButton(onClick = { isLocked = !isLocked }) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Lock,
                            contentDescription = if (isLocked) "Unlock note" else "Lock note"
                        )
                    }

                    // Pin toggle (UI only; persist later)
                    IconButton(onClick = {
                        isPinned = !isPinned
                        // TODO: persist pin state via viewModel.updateNote(...) if Note has a pinned field
                    }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Pin note",
                            tint = if (isPinned) Color.Yellow else LocalContentColor.current
                        )
                    }

                    // Reminder
                    IconButton(onClick = { showReminderDialog = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Add reminder")
                    }

                    // Done button (immediate save)
                    TextButton(onClick = {
                        commitBlocksToModelAndSave() // immediate on Done
                        onClose()
                    }) {
                        Text("Done")
                    }
                }
            )
        }
    ) { innerPadding ->

        // Reminder dialog (overlaid)
        if (showReminderDialog) {
            ReminderDialog(
                onDismiss = { showReminderDialog = false },
                onSave = { reminder ->
                    // You get the fully-populated ReminderData here.
                    // Persist/schedule as you like. For now we just close.
                    // Example: viewModel.saveReminderForNote(noteId, reminder)  <-- implement in your app
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

                // Title
                BasicTextField(
                    value = title,
                    onValueChange = {
                        if (!isLocked) {
                            title = it
                            // Title change should also be autosaved (debounced)
                            scheduleAutosave()
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

                // Render blocks sequentially
                for ((index, block) in blocks.withIndex()) {
                    when (block) {
                        is Block.Text -> {
                            // Ensure we have a corresponding TextFieldValue
                            val tfv = blockFieldValues.getOrNull(index) ?: TextFieldValue(block.text)
                            BasicTextField(
                                value = tfv,
                                onValueChange = { newTfV ->
                                    if (!isLocked) {
                                        // update UI state
                                        val updatedVals = blockFieldValues.toMutableList()
                                        if (index < updatedVals.size) updatedVals[index] = newTfV else updatedVals.add(newTfV)
                                        blockFieldValues = updatedVals

                                        // update underlying block text
                                        val newBlocks = blocks.toMutableList()
                                        newBlocks[index] = Block.Text(newTfV.text)
                                        blocks = newBlocks

                                        // track focus
                                        focusedBlockIndex = index
                                        focusedCursorOffset = newTfV.selection.start

                                        // debounced autosave
                                        scheduleAutosave()
                                    }
                                },
                                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .onKeyEvent { event ->
                                        // If user inserts a checklist using a keyboard shortcut in future, handle here.
                                        false
                                    },
                                decorationBox = { inner ->
                                    if (tfv.text.isEmpty()) Text(
                                        "Write something...",
                                        fontSize = 18.sp,
                                        color = Color.Gray
                                    )
                                    inner()
                                }
                            )
                        }

                        is Block.Checklist -> {
                            // checklist UI
                            val tfv = blockFieldValues.getOrNull(index) ?: TextFieldValue(block.text)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = block.checked,
                                    onCheckedChange = { checked ->
                                        if (!isLocked) {
                                            val newBlocks = blocks.toMutableList()
                                            newBlocks[index] = Block.Checklist(block.text, checked)
                                            blocks = newBlocks
                                            syncFieldValuesFromBlocks(blocks)
                                            // debounced autosave (per Option A: debounce all inputs)
                                            scheduleAutosave()
                                        }
                                    }
                                )

                                BasicTextField(
                                    value = tfv,
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

                                            // debounced autosave
                                            scheduleAutosave()
                                        }
                                    },
                                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                                    modifier = Modifier
                                        .weight(1f)
                                        .onKeyEvent { event ->
                                            if (isLocked) return@onKeyEvent true // swallow keys while locked

                                            // BACKSPACE on empty checklist -> delete checklist and merge per Option A
                                            if (event.key == Key.Backspace && event.type == KeyEventType.KeyUp && tfv.text.isEmpty()) {
                                                val newBlocks = blocks.toMutableList()

                                                // Remove the checklist itself
                                                newBlocks.removeAt(index)

                                                // Remove an immediately following empty text placeholder if present (we used "\n" earlier)
                                                if (index < newBlocks.size && newBlocks[index] is Block.Text && (newBlocks[index] as Block.Text).text == "\n") {
                                                    newBlocks.removeAt(index)
                                                }

                                                // Determine where to focus now:
                                                // Prefer previous block (index-1). If none, ensure at least one text block exists.
                                                val targetIndex = when {
                                                    newBlocks.isEmpty() -> {
                                                        newBlocks.add(Block.Text(""))
                                                        0
                                                    }

                                                    index - 1 >= 0 -> (index - 1).coerceAtMost(
                                                        newBlocks.lastIndex
                                                    )

                                                    else -> 0
                                                }

                                                blocks = newBlocks
                                                syncFieldValuesFromBlocks(blocks)

                                                // If target is Text -> place cursor at its end (merge target behavior)
                                                if (blocks[targetIndex] is Block.Text) {
                                                    val txt = (blocks[targetIndex] as Block.Text).text
                                                    // update selection for that block
                                                    blockFieldValues = blockFieldValues.toMutableList().also {
                                                        if (targetIndex < it.size) it[targetIndex] =
                                                            TextFieldValue(
                                                                txt,
                                                                selection = TextRange(txt.length)
                                                            )
                                                    }
                                                    focusedBlockIndex = targetIndex
                                                    focusedCursorOffset = txt.length
                                                } else {
                                                    // target is checklist -> focus its text end
                                                    val clText = (blocks[targetIndex] as Block.Checklist).text
                                                    blockFieldValues = blockFieldValues.toMutableList().also {
                                                        if (targetIndex < it.size) it[targetIndex] =
                                                            TextFieldValue(
                                                                clText,
                                                                selection = TextRange(clText.length)
                                                            )
                                                    }
                                                    focusedBlockIndex = targetIndex
                                                    focusedCursorOffset = clText.length
                                                }

                                                // debounced autosave
                                                scheduleAutosave()
                                                true
                                            }
                                            // ENTER in checklist -> add new checklist below
                                            else if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                                                val newBlocks = blocks.toMutableList()
                                                newBlocks.add(index + 1, Block.Checklist("", false))
                                                // Ensure a text block after for spacing (if desired)
                                                newBlocks.add(index + 2, Block.Text("\n"))
                                                blocks = newBlocks
                                                syncFieldValuesFromBlocks(blocks)
                                                // focus newly inserted checklist
                                                focusedBlockIndex = index + 1
                                                // ensure selection is at start
                                                blockFieldValues = blockFieldValues.toMutableList().also {
                                                    if (focusedBlockIndex < it.size) it[focusedBlockIndex] =
                                                        TextFieldValue(
                                                            "",
                                                            selection = TextRange(0)
                                                        )
                                                }
                                                // debounced autosave
                                                scheduleAutosave()
                                                true
                                            } else false
                                        },
                                    decorationBox = { inner ->
                                        if (tfv.text.isEmpty()) Text(
                                            "List item",
                                            fontSize = 18.sp,
                                            color = Color.Gray
                                        )
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }

            // Bottom toolbar (list button inserts a real checklist at cursor)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFB9A8E6), RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(onClick = {
                        if (!isLocked) imagePickerLauncher.launch("image/*")
                    }) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White)
                    }

                    IconButton(onClick = {
                        // Insert a real checklist at current cursor position (in focused block)
                        insertChecklistAtCursor()
                    }) {
                        Icon(Icons.Default.List, contentDescription = null, tint = Color.White)
                    }

                    IconButton(onClick = {
                        if (!isLocked) filePickerLauncher.launch("*/*")
                    }) {
                        Icon(
                            Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    var showMoreMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }

                    // simple More menu
                    if (showMoreMenu) {
                        Column(
                            modifier = Modifier
                                .padding(end = 16.dp, bottom = 70.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFEAE2FF),
                                            Color(0xFFF7F1FF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Delete note
                                        note?.let { viewModel.deleteNote(it.id); onClose() }
                                        showMoreMenu = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Delete")
                            }

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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMoreMenu = false }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Collaborator")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* -------------------------
   Reminder UI (full Version B: 3-step time flow + place tab)
   ------------------------- */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReminderDialog(
    onDismiss: () -> Unit,
    onSave: (ReminderData) -> Unit
) {
    val ctx = LocalContext.current

    var tab by remember { mutableStateOf(0) }

    var step by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val timeState = rememberTimePickerState()

    var repeatChoices by remember { mutableStateOf<List<String>>(emptyList()) }

    var placeText by remember { mutableStateOf("") }

    fun showDatePicker(onPicked: (Int, Int, Int) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(
            ctx,
            { _, y, m, d -> onPicked(y, m, d) },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(340.dp)
            ) {

                Text("Add reminder", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                TabRow(selectedTabIndex = tab) {
                    Tab(tab == 0, { tab = 0 }) { Text("Time") }
                    Tab(tab == 1, { tab = 1 }) { Text("Place") }
                }

                Spacer(Modifier.height(16.dp))

                // ----------------------------
                // TIME TAB (3 STEPS)
                // ----------------------------
                if (tab == 0) {

                    Text("Step ${step + 1} of 3", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    when (step) {

                        // STEP 1 — PICK DATE
                        0 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {

                                    Text("Choose date", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        selectedDate?.let { Date(it).toString() } ?: "No date selected"
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Button(onClick = {
                                        showDatePicker { y, m, d ->
                                            val cal = Calendar.getInstance()
                                            cal.set(y, m, d, 0, 0)
                                            selectedDate = cal.timeInMillis
                                        }
                                    }) {
                                        Text("Pick date")
                                    }

                                    if (selectedDate != null) {
                                        TextButton(onClick = { selectedDate = null }) {
                                            Text("Clear")
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2 — PICK TIME
                        1 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Choose time", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(8.dp))

                                    TimePicker(state = timeState)
                                }
                            }
                        }

                        // STEP 3 — REPEAT OPTIONS
                        2 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {

                                    Text("Repeat options", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(12.dp))

                                    var selected by remember { mutableStateOf(repeatChoices.toMutableSet()) }

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        // Everyday
                                        FilterChip(
                                            selected = "Everyday" in selected,
                                            onClick = {
                                                selected.clear()
                                                selected.add("Everyday")
                                                repeatChoices = selected.toList()
                                            },
                                            label = { Text("Everyday") }
                                        )

                                        val days = listOf(
                                            "Monday", "Tuesday", "Wednesday",
                                            "Thursday", "Friday", "Saturday", "Sunday"
                                        )

                                        days.forEach { day ->
                                            FilterChip(
                                                selected = day in selected,
                                                onClick = {
                                                    if (day in selected) selected.remove(day)
                                                    else selected.add(day)

                                                    selected.remove("Everyday")
                                                    repeatChoices = selected.toList()
                                                },
                                                label = { Text(day.take(3)) }
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))
                                    Text("Selected: ${repeatChoices.ifEmpty { listOf("None") }.joinToString()}")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        if (step > 0) {
                            OutlinedButton(onClick = { step-- }) { Text("Back") }
                        } else Spacer(modifier = Modifier.width(1.dp))

                        if (step < 2) {
                            Button(onClick = { step++ }) { Text("Next") }
                        }
                    }
                }

                // ----------------------------
                // PLACE TAB
                // ----------------------------
                if (tab == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Place", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = placeText,
                                onValueChange = { placeText = it },
                                label = { Text("Location name or address") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ----------------------------
                // SAVE
                // ----------------------------
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        val cal = Calendar.getInstance()

                        if (selectedDate != null) cal.timeInMillis = selectedDate!!

                        cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        cal.set(Calendar.MINUTE, timeState.minute)
                        cal.set(Calendar.SECOND, 0)

                        onSave(
                            ReminderData(
                                dateMillis = selectedDate,
                                timeMillis = cal.timeInMillis,
                                repeat = repeatChoices,
                                location = placeText.ifBlank { null }
                            )
                        )
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
