package com.example.wifty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape

@Composable
fun AddFolderScreen() {

    var title by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Swift Notes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Title")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Enter title here...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tag #")
        OutlinedTextField(
            value = tag,
            onValueChange = { tag = it },
            placeholder = { Text("Add tag here...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Choose Color")
        Spacer(modifier = Modifier.height(16.dp))

// List of sample colors
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.surfaceVariant
        )

        var selectedColor by remember { mutableStateOf(colors[0]) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .then(
                            if (selectedColor == color)
                                Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier
                        )
                        .background(color, CircleShape)
                        .clickable { selectedColor = color }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { /* use title and tag here */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Create")
        }
    }
}
