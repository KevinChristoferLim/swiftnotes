package com.example.wifty.ui.screens.modules

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Pure editor helpers for checklist insertion / deletion / splitting behaviour.
 */

/** Result returned after performing an edit operation. */
data class EditResult(
    val blocks: List<Block>,
    val fieldValues: List<TextFieldValue>,
    val focusedIndex: Int,
    val focusedCursorOffset: Int
)

/** Sync TextFieldValue list to match blocks; always returns a valid TextFieldValue per block. */
fun syncFieldValuesFromBlocksPure(currentBlocks: List<Block>): List<TextFieldValue> {
    return currentBlocks.map { b ->
        when (b) {
            is Block.Text -> TextFieldValue(b.text)
            is Block.Checklist -> TextFieldValue(b.text, selection = TextRange(b.text.length))

            // Added to satisfy exhaustiveness
            is Block.FileBlock -> TextFieldValue("")
            is Block.ImageBlock -> TextFieldValue("")
        }
    }
}

/**
 * Insert a checklist at the cursor position inside the given focused text block.
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
        newBlocksMutable.removeAt(idx)

        if (currentLine.isBlank()) {
            val before = text.substring(0, lineStart)
            val after = text.substring(lineEnd)

            if (before.isNotEmpty()) newBlocksMutable.add(idx, Block.Text(before))
            newBlocksMutable.add(idx + if (before.isNotEmpty()) 1 else 0, Block.Checklist("", false))
            newBlocksMutable.add(idx + if (before.isNotEmpty()) 2 else 1, Block.Text("\n" + after))
        } else {
            val before = text.substring(0, lineEnd)
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
        // Focused on checklist
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

    // Remove checklist
    val newBlocks = blocks.toMutableList()
    newBlocks.removeAt(idx)

    if (newBlocks.isEmpty()) {
        val single = listOf(Block.Text(""))
        val fvs = syncFieldValuesFromBlocksPure(single)
        return EditResult(single, fvs, 0, 0)
    }

    val targetIndex = if (idx - 1 >= 0) idx - 1 else 0
    val fvs = syncFieldValuesFromBlocksPure(newBlocks)

    val target = newBlocks[targetIndex]
    val cursorPos = when (target) {
        is Block.Text -> target.text.length
        is Block.Checklist -> target.text.length

        // Added to avoid non-exhaustive when
        is Block.FileBlock -> 0
        is Block.ImageBlock -> 0
    }

    return EditResult(
        blocks = newBlocks.toList(),
        fieldValues = fvs,
        focusedIndex = targetIndex,
        focusedCursorOffset = cursorPos
    )
}

/**
 * Handle Enter pressed inside a checklist.
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
