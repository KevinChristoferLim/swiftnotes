package com.example.wifty.ui.screens.modules

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Pure editor helpers for checklist insertion / deletion / splitting behaviour.
 *
 * These functions accept the current lists and indices and return a data class with
 * the updated lists and focus/selection targets. This style keeps the logic testable
 * and easy to call from a Composable's stateful scope.
 */

/** Result returned after performing an edit operation. */
data class EditResult(
    val blocks: List<Block>,
    val fieldValues: List<TextFieldValue>,
    val focusedIndex: Int,
    val focusedCursorOffset: Int
)

/** Sync TextFieldValue list to match blocks; selection default placed at end for new checklist. */
fun syncFieldValuesFromBlocksPure(currentBlocks: List<Block>): List<TextFieldValue> {
    return currentBlocks.map { b ->
        when (b) {
            is Block.Text -> TextFieldValue(b.text)
            is Block.Checklist -> TextFieldValue(b.text, selection = TextRange(b.text.length))
        }
    }
}

/**
 * Insert a checklist at the cursor position inside the given focused text block.
 *
 * - If focused block is Text: tries to split the text on the current line and insert checklist.
 * - If focused block is Checklist: inserts a new checklist below it and an empty text line after.
 *
 * Returns EditResult with updated blocks, fieldValues, and new focus.
 */
fun insertChecklistAtCursorPure(
    blocks: List<Block>,
    blockFieldValues: List<TextFieldValue>,
    focusedBlockIndex: Int,
    focusedCursorOffset: Int,
    isLocked: Boolean
): EditResult {
    if (isLocked) {
        return EditResult(blocks, blockFieldValues, focusedBlockIndex, focusedCursorOffset)
    }

    if (blocks.isEmpty()) {
        val newBlocks = listOf(Block.Checklist("", false), Block.Text(""))
        val fvs = syncFieldValuesFromBlocksPure(newBlocks)
        return EditResult(newBlocks, fvs, 0, 0)
    }

    val idx = focusedBlockIndex.coerceIn(0, blocks.lastIndex)
    val focusedBlock = blocks[idx]
    if (focusedBlock is Block.Text) {
        val tfv = blockFieldValues.getOrNull(idx) ?: TextFieldValue(focusedBlock.text)
        val cursor = focusedCursorOffset.coerceIn(0, tfv.text.length)

        val text = focusedBlock.text
        val lineStart = text.lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }

        val currentLine = text.substring(lineStart, lineEnd)

        val newBlocksMutable = blocks.toMutableList()
        // remove the original text block; we'll add back appropriate fragments
        newBlocksMutable.removeAt(idx)

        if (currentLine.isBlank()) {
            val before = text.substring(0, lineStart)
            val after = text.substring(lineEnd)

            if (before.isNotEmpty()) newBlocksMutable.add(idx, Block.Text(before))
            newBlocksMutable.add(idx + if (before.isNotEmpty()) 1 else 0, Block.Checklist("", false))
            newBlocksMutable.add(idx + if (before.isNotEmpty()) 2 else 1, Block.Text("\n" + after))
        } else {
            val before = text.substring(0, lineEnd) // includes current line
            val after = text.substring(lineEnd)
            if (before.isNotEmpty()) newBlocksMutable.add(idx, Block.Text(before))
            newBlocksMutable.add(idx + if (before.isNotEmpty()) 1 else 0, Block.Checklist("", false))
            newBlocksMutable.add(idx + if (before.isNotEmpty()) 2 else 1, Block.Text("\n" + after))
        }

        val normalized = newBlocksMutable.toList()
        val fvs = syncFieldValuesFromBlocksPure(normalized)
        val newIndex = (idx + 1).coerceAtMost(normalized.lastIndex)
        return EditResult(normalized, fvs, newIndex, 0)
    } else {
        // Focused on checklist: insert checklist after it + ensure a spacer text.
        val newBlocks = blocks.toMutableList()
        newBlocks.add(idx + 1, Block.Checklist("", false))
        newBlocks.add(idx + 2, Block.Text("\n"))
        val normalized = newBlocks.toList()
        val fvs = syncFieldValuesFromBlocksPure(normalized)
        return EditResult(normalized, fvs, idx + 1, 0)
    }
}

/**
 * Handle backspace on an empty checklist (delete it and merge appropriately).
 *
 * If the checklist is removed, this function will:
 * - remove checklist block
 * - remove a following empty text placeholder if present
 * - compute new focus target and return updated blocks and fieldValues
 */
fun deleteChecklistOnBackspacePure(
    blocks: List<Block>,
    blockFieldValues: List<TextFieldValue>,
    checklistIndex: Int
): EditResult {
    val idx = checklistIndex.coerceIn(0, blocks.lastIndex)
    if (blocks[idx] !is Block.Checklist) {
        return EditResult(blocks, blockFieldValues, checklistIndex, 0)
    }

    val newBlocks = blocks.toMutableList()
    newBlocks.removeAt(idx)

    // Remove immediate following "\n" text placeholder if present
    if (idx < newBlocks.size && newBlocks[idx] is Block.Text && (newBlocks[idx] as Block.Text).text == "\n") {
        newBlocks.removeAt(idx)
    }

    if (newBlocks.isEmpty()) {
        val resultBlocks = listOf(Block.Text(""))
        val fvs = syncFieldValuesFromBlocksPure(resultBlocks)
        return EditResult(resultBlocks, fvs, 0, 0)
    }

    val targetIndex = when {
        idx - 1 >= 0 -> (idx - 1).coerceAtMost(newBlocks.lastIndex)
        else -> 0
    }

    val fvs = syncFieldValuesFromBlocksPure(newBlocks)
    val targetBlock = newBlocks[targetIndex]
    val cursor = when (targetBlock) {
        is Block.Text -> (targetBlock.text.length)
        is Block.Checklist -> (targetBlock.text.length)
    }

    return EditResult(newBlocks, fvs, targetIndex, cursor)
}

/**
 * Handle Enter pressed inside a checklist:
 * - insert new empty checklist below and a spacer text after it
 */
fun enterInChecklistPure(
    blocks: List<Block>,
    blockFieldValues: List<TextFieldValue>,
    checklistIndex: Int
): EditResult {
    val idx = checklistIndex.coerceIn(0, blocks.lastIndex)
    if (blocks[idx] !is Block.Checklist) {
        return EditResult(blocks, blockFieldValues, checklistIndex, 0)
    }

    val newBlocks = blocks.toMutableList()
    newBlocks.add(idx + 1, Block.Checklist("", false))
    newBlocks.add(idx + 2, Block.Text("\n"))
    val normalized = newBlocks.toList()
    val fvs = syncFieldValuesFromBlocksPure(normalized)
    val newIndex = (idx + 1).coerceAtMost(normalized.lastIndex)
    return EditResult(normalized, fvs, newIndex, 0)
}
