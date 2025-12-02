package com.example.wifty.ui.screens.AccountManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DeleteAccountDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Are you sure you want to delete your account?",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Deleting your account will permanently remove all data.",
                    color = Color.Red.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4F))
                    ) {
                        Text("Yes", color = Color.White)
                    }
                }
            }
        }
    }
}
