package com.example.wifty.ui.screens.AccountManagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wifty.R
import com.example.wifty.ui.screens.login.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val user = uiState.user

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showPasswordWarningDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.sample_profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))

            Text(user?.username ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(user?.email ?: "", color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onNavigateToEditProfile,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Edit Profile")
            }

            Spacer(Modifier.height(40.dp))

            ProfileMenuItem(Icons.Outlined.Lock, "Change Password") {
                showPasswordWarningDialog = true
            }

            ProfileMenuItem(Icons.Outlined.Notifications, "Notifications") {
                showNotificationSheet = true
            }

            Divider(Modifier.padding(vertical = 10.dp))

            ProfileMenuItem(Icons.Outlined.Delete, "Delete Account", isDestructive = true) {
                showDeleteDialog = true
            }

            ProfileMenuItem(Icons.Outlined.ExitToApp, "Logout", isDestructive = true) {
                showLogoutDialog = true
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Log Out",
            message = "Are you sure you want to log out from the application?",
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Account",
            message = "Are you sure you want to delete your account? This action is irreversible and all your data will be lost.",
            onConfirm = {
                showDeleteDialog = false
                authViewModel.deleteAccount()
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showPasswordWarningDialog) {
        PasswordWarningDialog(
            onDismiss = { showPasswordWarningDialog = false },
            onConfirm = {
                showPasswordWarningDialog = false
                onNavigateToChangePassword()
            }
        )
    }

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = Color.White
        ) {
            NotificationSettings()
        }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(message, textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF6750A4))
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (title == "Delete Account") Color.Red else Color(0xFF6750A4)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Yes")
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordWarningDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFFFFF3E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF9800)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Password Change", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Changing your password means your old password will be replaced, and you will need to sign in again. Do you agree?",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF6750A4))
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Yes")
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettings() {
    Column(Modifier.padding(24.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Email Notifications", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            var emailSwitch by remember { mutableStateOf(true) }
            Switch(
                checked = emailSwitch,
                onCheckedChange = { emailSwitch = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF6750A4),
                    checkedTrackColor = Color(0xFFEADDFF)
                )
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Push Notifications", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            var pushSwitch by remember { mutableStateOf(true) }
            Switch(
                checked = pushSwitch,
                onCheckedChange = { pushSwitch = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF6750A4),
                    checkedTrackColor = Color(0xFFEADDFF)
                )
            )
        }
        Spacer(Modifier.height(50.dp))
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color.Red else Color.Black
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color.Red else Color.Black,
            modifier = Modifier.weight(1f)
        )
        if (!isDestructive) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
