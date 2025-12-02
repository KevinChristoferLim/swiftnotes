package com.example.wifty.ui.screens.modules

import android.util.Base64
import com.example.wifty.model.ChecklistItem
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

// Marker format: [[CHECKLIST:BASE64_TEXT:0_or_1]]
private val CHECKLIST_REGEX = Pattern.compile("\\[\\[CHECKLIST:([A-Za-z0-9+/=]+):(0|1)]]")

/** Domain model used by the UI layer to represent content blocks. */
sealed class Block {
    data class Text(var text: String) : Block()
    data class Checklist(var text: String, var checked: Boolean) : Block()
}

/** Reminder data DTO used by the reminder UI. */
data class ReminderData(
    val dateMillis: Long? = null,
    val timeMillis: Long? = null,
    val repeat: List<String> = emptyList(),
    val location: String? = null
)

/** Encode string to base64 without newlines for marker embedding. */
fun encodeForMarker(s: String): String {
    val bytes = s.toByteArray(StandardCharsets.UTF_8)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

/** Decode base64 marker value; safe fallback to empty string on error. */
fun decodeMarkerValue(b64: String): String {
    return try {
        val bytes = Base64.decode(b64, Base64.NO_WRAP)
        String(bytes, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        ""
    }
}

/** Parse serialized content (with checklist markers) into UI Blocks. */
fun parseContentToBlocks(content: String): List<Block> {
    val matcher = CHECKLIST_REGEX.matcher(content)
    val result = mutableListOf<Block>()
    var lastEnd = 0
    while (matcher.find()) {
        val start = matcher.start()
        if (start > lastEnd) {
            val textSegment = content.substring(lastEnd, start)
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

/** Serialize UI Blocks back into storage content string (with markers). */
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

/**
 * Normalize blocks before saving:
 * - ensures Text blocks don't embed checklist markers (split them into explicit Block.Checklist)
 * - returns non-empty list (at least one Text block)
 */
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

    if (out.isEmpty()) out.add(Block.Text(""))

    return out
}

/** Helper to build ChecklistItem list from normalized blocks (for model compatibility). */
fun blocksToChecklistItems(blocks: List<Block>, noteId: String): List<ChecklistItem> {
    return blocks.mapIndexedNotNull { idx, b ->
        when (b) {
            is Block.Checklist -> ChecklistItem(
                text = b.text,
                isChecked = b.checked,
                noteId = noteId,
                order = idx
            )
            else -> null
        }
    }
}
