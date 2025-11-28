package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.github.skydoves.colorpicker.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderScreen() {

    var title by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }

    var showColorPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color(0xFF4B63FF)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            "Swift Notes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Title")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter title here…") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tag #")
        OutlinedTextField(
            value = tag,
            onValueChange = { tag = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Add tag here…") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Choose Color")

        Spacer(modifier = Modifier.height(16.dp))

        // --- COLOR BOX THAT OPENS PICKER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(selectedColor, RoundedCornerShape(8.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable { showColorPicker = true }
        )

        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                confirmButton = {
                    TextButton(onClick = { showColorPicker = false }) {
                        Text("Done")
                    }
                },
                title = { Text("Pick a Color") },
                text = {
                    val controller = rememberColorPickerController()

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        HsvColorPicker(
                            modifier = Modifier
                                .size(250.dp)
                                .padding(8.dp),
                            controller = controller,
                            onColorChanged = { envelope ->
                                selectedColor = envelope.color
                            }
                        )

                        AlphaSlider(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            controller = controller
                        )

                        BrightnessSlider(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            controller = controller
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { /* Save folder */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Create")
        }
    }
}
