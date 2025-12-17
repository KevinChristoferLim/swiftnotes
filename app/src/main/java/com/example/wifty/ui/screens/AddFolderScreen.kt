package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wifty.model.Folder
import com.example.wifty.viewmodel.FolderViewModel
import com.github.skydoves.colorpicker.compose.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderScreen(
    folderViewModel: FolderViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(generateRandomColor()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Folder") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(24.dp),
        ) {

            Text("Title")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter title here…") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Description (optional)")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a description…") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Folder Color")

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(selectedColor, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { showColorPicker = true }
            )

            if (showColorPicker) {
                ColorPickerDialog(
                    initialColor = selectedColor,
                    onDismiss = { showColorPicker = false },
                    onColorSelected = { color ->
                        selectedColor = color
                        showColorPicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newFolder = Folder(
                            id = UUID.randomUUID().toString(), // Server will replace this
                            title = title,
                            description = description.ifBlank { null },
                            colorLong = selectedColor.toArgb().toLong(),
                        )
                        folderViewModel.createFolder(newFolder) { _ ->
                            onSaved()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = title.isNotBlank()
            ) {
                Text("Create Folder", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var tempColor by remember { mutableStateOf(initialColor) }
    val controller = rememberColorPickerController()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onColorSelected(tempColor) }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Choose a Color") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(10.dp),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { envelope ->
                        tempColor = envelope.color
                    }
                )
                Spacer(Modifier.height(10.dp))
                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(35.dp),
                    controller = controller,
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(35.dp),
                    controller = controller,
                )
            }
        }
    )
}

private fun generateRandomColor(): Color {
    val random = java.util.Random()
    return Color(
        red = random.nextFloat(),
        green = random.nextFloat(),
        blue = random.nextFloat(),
        alpha = 1f
    )
}
