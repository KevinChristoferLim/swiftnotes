package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.BorderStroke
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
 * The countdown is accurate and shows time remaining including seconds.
 */
@Composable
fun LiveReminderDisplay(
    reminder: ReminderData?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFE91E63),
    textColor: Color = Color.White,
    compact: Boolean = false
) {
    if (reminder == null) return

    // State to trigger recomposition every second for live countdown
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Use LaunchedEffect to update the time every second
    LaunchedEffect(key1 = reminder) {
        while (true) {
            delay(1000) // Update every second
            currentTime = System.currentTimeMillis()
        }
    }

    // Pass currentTime to formatReminder so Compose knows it needs to re-calculate 
    // when currentTime changes.
    val displayText = formatReminder(reminder, currentTime)
    if (displayText == null) return

    val isOverdue = displayText == "Overdue"
    val displayColor = if (isOverdue) Color.Red else backgroundColor

    if (compact) {
        // Compact inline version (for note cards)
        Surface(
            color = displayColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
            modifier = modifier,
            border = BorderStroke(1.dp, displayColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = displayColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    displayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = displayColor
                )
            }
        }
    } else {
        // Full version (for detailed display)
        Surface(
            color = displayColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.5.dp, displayColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Reminder set",
                    modifier = Modifier.size(24.dp),
                    tint = displayColor
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isOverdue) "REMINDER OVERDUE" else "UPCOMING REMINDER",
                        fontSize = 10.sp,
                        color = displayColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        displayText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = displayColor
                    )
                }
            }
        }
    }
}
