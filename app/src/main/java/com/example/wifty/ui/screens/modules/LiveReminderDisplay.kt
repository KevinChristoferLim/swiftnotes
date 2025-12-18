package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * LiveReminderDisplay shows a reminder with a live countdown that updates every second.
 * The countdown is accurate and shows time remaining (e.g., "In 2 hours", "In 45m").
 */
@Composable
fun LiveReminderDisplay(
    reminder: ReminderData?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4B63FF),
    textColor: Color = Color.White,
    compact: Boolean = false
) {
    if (reminder == null) return

    // State to trigger recomposition every second for live countdown
    var timeTrigger by remember { mutableStateOf(System.currentTimeMillis()) }

    // Use LaunchedEffect to update the time every second
    LaunchedEffect(key1 = reminder) {
        while (true) {
            delay(1000) // Update every second
            timeTrigger = System.currentTimeMillis()
        }
    }

    val displayText = formatReminder(reminder)
    if (displayText == null) return

    if (compact) {
        // Compact inline version (for note cards)
        Surface(
            color = backgroundColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = backgroundColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    displayText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = backgroundColor
                )
            }
        }
    } else {
        // Full version (for detailed display)
        Surface(
            color = backgroundColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Reminder set",
                    modifier = Modifier.size(20.dp),
                    tint = backgroundColor
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Reminder",
                        fontSize = 12.sp,
                        color = backgroundColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        displayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = backgroundColor
                    )
                }
            }
        }
    }
}
