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
import androidx.compose.material.icons.filled.Warning // Imported for the warning icon
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wifty.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToChangePassword: () -> Unit // 👈 NEW CALLBACK ADDED
) {
    // State for Dialogs and Sheets
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showPasswordWarningDialog by remember { mutableStateOf(false) } // 👈 NEW STATE

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

            // --- 1. PROFILE HEADER ---
            Image(
                painter = painterResource(id = R.drawable.sample_profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))

            Text("Ananta Valentina", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("anantavlntina@gmail.com", color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(16.dp))

            // Edit Profile Button
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

            // --- 2. SETTINGS LIST ---

            // 👇 UPDATED: Change Password clicks open the Warning Dialog
            ProfileMenuItem(Icons.Outlined.Lock, "Change Password") {
                showPasswordWarningDialog = true
            }

            ProfileMenuItem(Icons.Outlined.Info, "Text Size") { /* Handle Text Size */ }

            // Notification Item -> Opens Sheet
            ProfileMenuItem(Icons.Outlined.Notifications, "Notifications") {
                showNotificationSheet = true
            }

            Divider(Modifier.padding(vertical = 10.dp))

            // Logout Item -> Opens Dialog
            ProfileMenuItem(Icons.Outlined.ExitToApp, "Logout", isDestructive = true) {
                showLogoutDialog = true
            }
        }
    }

    // --- 3. LOGOUT DIALOG ---
    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Log Out", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Are you sure you want to log out from the application?",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color(0xFF6750A4))
                        }
                        Spacer(Modifier.width(16.dp))
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
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

    // --- 4. PASSWORD WARNING DIALOG (NEW) ---
    if (showPasswordWarningDialog) {
        Dialog(onDismissRequest = { showPasswordWarningDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Yellow Circle with Warning Icon
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { showPasswordWarningDialog = false },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color(0xFF6750A4))
                        }
                        Spacer(Modifier.width(16.dp))
                        Button(
                            onClick = {
                                showPasswordWarningDialog = false
                                onNavigateToChangePassword() // Navigate to ForgotPassword Screen
                            },
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

    // --- 5. NOTIFICATION BOTTOM SHEET ---
    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = Color.White
        ) {
            Column(Modifier.padding(24.dp)) {
                // Email Switch
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
                // Push Switch
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
                Spacer(Modifier.height(50.dp)) // Extra padding at bottom
            }
        }
    }
}

// Helper Composable for List Items
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
        // Only show arrow if it's NOT the destructive (Logout) button
        if (!isDestructive) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}