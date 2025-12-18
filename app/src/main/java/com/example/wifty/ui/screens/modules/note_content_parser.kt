package com.example.wifty.ui.screens.modules

import android.util.Base64
import com.example.wifty.model.ChecklistItem
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

// Marker formats:
// [[CHECKLIST:BASE64_TEXT:0_or_1]]
// [[IMAGE:BASE64_URI]]
// [[FILE:BASE64_URI:BASE64_FILENAME:SIZE_BYTES]]
private val CHECKLIST_REGEX = Pattern.compile("\\[\\[CHECKLIST:([A-Za-z0-9+/=]*):?(0|1)]]")
private val IMAGE_REGEX = Pattern.compile("\\[\\[IMAGE:([A-Za-z0-9+/=]+)]]")
private val FILE_REGEX = Pattern.compile("\\[\\[FILE:([A-Za-z0-9+/=]+):([A-Za-z0-9+/=]+):([0-9]+)]]")

/** Domain model used by the UI layer to represent content blocks. */
sealed class Block {
    data class Text(var text: String) : Block()
    data class Checklist(var text: String, var checked: Boolean) : Block()
    data class ImageBlock(val uri: String) : Block()
    data class FileBlock(val uri: String, val filename: String, val sizeBytes: Long) : Block()
}

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

/** Parse serialized content (with markers) into UI Blocks. */
fun parseContentToBlocks(content: String): List<Block> {
    // We'll scan the content and detect markers (CHECKLIST / IMAGE / FILE)
    // in reading order and emit blocks preserving other text segments as Text blocks.
    val result = mutableListOf<Block>()
    var cursor = 0
    val combinedPattern = Pattern.compile(
        "\\[\\[(CHECKLIST|IMAGE|FILE):.*?]]",
        Pattern.DOTALL
    )
    val matcher = combinedPattern.matcher(content)
    while (matcher.find()) {
        val start = matcher.start()
        if (start > cursor) {
            val textSegment = content.substring(cursor, start)
            result.add(Block.Text(textSegment))
        }
        val marker = matcher.group()
        // Try checklist first
        val checklistMatcher = CHECKLIST_REGEX.matcher(marker)
        if (checklistMatcher.matches()) {
            val b64 = checklistMatcher.group(1) ?: ""
            val checked = checklistMatcher.group(2) == "1"
            val checklistText = decodeMarkerValue(b64)
            result.add(Block.Checklist(checklistText, checked))
            cursor = matcher.end()
            continue
        }
        // image
        val imageMatcher = IMAGE_REGEX.matcher(marker)
        if (imageMatcher.matches()) {
            val b64uri = imageMatcher.group(1) ?: ""
            val uri = decodeMarkerValue(b64uri)
            result.add(Block.ImageBlock(uri))
            cursor = matcher.end()
            continue
        }
        // file
        val fileMatcher = FILE_REGEX.matcher(marker)
        if (fileMatcher.matches()) {
            val b64uri = fileMatcher.group(1) ?: ""
            val b64fn = fileMatcher.group(2) ?: ""
            val sizeStr = fileMatcher.group(3) ?: "0"
            val uri = decodeMarkerValue(b64uri)
            val filename = decodeMarkerValue(b64fn)
            val sizeBytes = try { sizeStr.toLong() } catch (_: Exception) { 0L }
            result.add(Block.FileBlock(uri, filename, sizeBytes))
            cursor = matcher.end()
            continue
        }
        // If somehow marker didn't match expected formats, treat as text
        result.add(Block.Text(marker))
        cursor = matcher.end()
    }
    if (cursor < content.length) {
        result.add(Block.Text(content.substring(cursor)))
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
            is Block.ImageBlock -> {
                val enc = encodeForMarker(b.uri)
                sb.append("[[IMAGE:").append(enc).append("]]")
            }
            is Block.FileBlock -> {
                val encUri = encodeForMarker(b.uri)
                val encFn = encodeForMarker(b.filename)
                val size = b.sizeBytes
                sb.append("[[FILE:").append(encUri).append(":").append(encFn).append(":").append(size).append("]]")
            }
        }
    }
    return sb.toString()
}

/**
 * Normalize blocks before saving:
 * - ensures Text blocks don't embed checklist markers (split them into explicit Block.Checklist)
 * - preserves IMAGE / FILE blocks intact
 * - returns non-empty list (at least one Text block)
 */
fun normalizeBlocksForSave(inputBlocks: List<Block>): List<Block> {
    val out = mutableListOf<Block>()
    for (b in inputBlocks) {
        when (b) {
            is Block.Text -> {
                val text = b.text
                // split out checklist markers inside text (existing behavior),
                // but preserve any other parts as Text
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
            }
            // Keep existing block types as-is, including new image/file blocks
            else -> out.add(b)
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
