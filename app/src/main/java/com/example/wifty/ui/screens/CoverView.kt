package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun CoverView() {
    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(Color(0xFF334C80), Color(0xFF8066A3), Color(0xFFD9DAE5)))),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SWIFT", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("NOTES", color = Color.White)
        }
    }
}
