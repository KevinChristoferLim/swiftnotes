package com.example.wifty.ui.screens.AccountManagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifty.R

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDeleteConfirm: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val profilePainter = runCatching {
        painterResource(id = R.drawable.sample_profile)
    }.getOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF6F3FF), Color.White)
                )
            )
            .padding(20.dp)
    ) {
        Text(
            "‹",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable { onBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Settings", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(24.dp))

            if (profilePainter != null) {
                Image(
                    painter = profilePainter,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text("Ananta Valentina", fontSize = 20.sp)
            Text("anantavalentina@gmail.com", color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = {}) {
                Text("Edit Profile")
            }

            Spacer(Modifier.height(24.dp))

            SettingItem(Icons.Default.Lock, "Change Password") {}
            SettingItem(Icons.Default.Notifications, "Notifications") {}

            Divider(Modifier.padding(vertical = 16.dp))

            SettingItem(Icons.Default.ExitToApp, "Logout") { onLogout() }

            SettingItem(Icons.Default.Delete, "Delete Account") {
                showDeleteDialog = true
            }
        }

        if (showDeleteDialog) {
            DeleteAccountDialog(
                onCancel = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    onDeleteConfirm()
                }
            )
        }
    }
}
