package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

/**
 * Composables for rendering single Block types.
 *
 * These are intentionally small and stateless — they expose events to the parent which
 * remains the single source of truth for lists and focus.
 */

/** Text-only block editor. */
@Composable
fun TextBlockEditor(
    value: TextFieldValue,
    isLocked: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { if (!isLocked) onValueChange(it) },
        textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
        modifier = modifier
            .padding(vertical = 4.dp)
    ) { inner ->
        if (value.text.isEmpty()) {
            androidx.compose.material3.Text(
                "Write something...",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
        inner()
    }
}

/** Checklist block editor row: checkbox + editable text. */
@Composable
fun ChecklistBlockEditor(
    value: TextFieldValue,
    checked: Boolean,
    isLocked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    onKeyEvent: (KeyEvent) -> Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (!isLocked) onCheckedChange(it) }
        )

        BasicTextField(
            value = value,
            onValueChange = { if (!isLocked) onValueChange(it) },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
            modifier = Modifier
                .weight(1f),
            decorationBox = { inner ->
                if (value.text.isEmpty()) {
                    androidx.compose.material3.Text("List item", fontSize = 18.sp, color = Color.Gray)
                }
                inner()
            }
        )
    }
}
