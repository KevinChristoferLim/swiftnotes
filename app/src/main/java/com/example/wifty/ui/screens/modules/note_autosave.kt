package com.example.wifty.ui.screens.modules

import com.example.wifty.model.Note
import com.example.wifty.viewmodel.NotesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Autosave helpers which keep the same behavior as original:
 * - scheduleAutosave: debounced 1 second delay to call commit function
 * - commitBlocksToModelAndSave: normalize blocks, serialize, update note via viewModel
 *
 * NOTE: These helpers accept state and return updated state (or execute update via viewModel).
 */

/** Schedule a debounced autosave: returns a Job (the scheduled task). */
fun scheduleAutosave(
    coroutineScope: CoroutineScope,
    previousJob: Job?,
    commitAction: suspend () -> Unit
): Job {
    previousJob?.cancel()
    return coroutineScope.launch {
        delay(1000L)
        commitAction()
    }
}

/**
 * Commit in-memory blocks to the Note model and persist via viewModel.
 * Returns updated Note if successful, or null otherwise.
 *
 * This function normalizes blocks (so Text doesn't embed checklist markers),
 * converts to storage content string and updates note via viewModel.updateNote(token, updated).
 */
fun commitBlocksToModelAndSave(
    token: String,
    originalNote: Note?,
    titleText: String,
    blocks: List<Block>,
    colorLong: Long,
    viewModel: NotesViewModel
): Note? {
    if (originalNote == null) return null

    val normalized = normalizeBlocksForSave(blocks)
    // prepare checklist items for the model
    val checklist = blocksToChecklistItems(normalized, originalNote.id)
    val contentString = serializeBlocksToContent(normalized)

    val updated = originalNote.copy(
        title = titleText,
        content = contentString,
        colorLong = colorLong,
        checklist = checklist,
        updatedAt = System.currentTimeMillis() // Ensure updatedAt is refreshed for others to see
    )

    // persist via viewModel (fire & forget; viewModel implementation handles threading)
    viewModel.updateNote(token, updated)

    return updated
}
