package com.example.wifty.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 1. I renamed this back to 'LandingScreen' so it doesn't conflict
@Composable
fun LandingScreen(
    onCreateNewNote: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFFEDE9FF), Color.White))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Start Your Journey", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Every big step starts with a small step.\nNote your first idea and start your journey!",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            FloatingActionButton(onClick = onCreateNewNote) {
                Text("+")
            }
        }

        // Navigation Buttons
        Button(
            onClick = onOpenFolders,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) { Text("Folders") }

        Button(
            onClick = onOpenProfile,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) { Text("Profile") }
    }
}