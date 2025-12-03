package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Bottom toolbar composable: contains icon actions (image, checklist, file, more).
 * It only emits UI events; the parent composes actual launchers & logic.
 */
@Composable
fun NoteBottomBar(
    onImageClick: () -> Unit,
    onInsertChecklistClick: () -> Unit,
    onFileClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(start = 5.dp, bottom = 10.dp)
            .wrapContentSize(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFFB9A8E6), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onImageClick) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White)
            }

            IconButton(onClick = onInsertChecklistClick) {
                Icon(Icons.Default.List, contentDescription = null, tint = Color.White)
            }

            IconButton(onClick = onFileClick) {
                Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color.White)
            }

            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
            }
        }
    }
}

/**
 * Floating More menu UI - simple column with clickable rows.
 * It delegates action handling back to the parent via lambdas.
 */
@Composable
fun FloatingMoreMenu(
    onDeleteClick: () -> Unit,
    onCopyClick: () -> Unit,
    onCollaboratorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .wrapContentSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(180.dp)
        ) {
            MenuItem(icon = Icons.Default.Delete, text = "Delete", onClick = onDeleteClick)
            MenuItem(icon = Icons.Default.Info, text = "Copy", onClick = onCopyClick)
            MenuItem(icon = Icons.Default.Share, text = "Collaborators", onClick = onCollaboratorClick)
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = Color.Black)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}


/**
 * Delete confirmation dialog; self-contained and simple.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(260.dp).padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF5A3EAA))
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "Are you sure you want to delete this note?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4E3A82)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Deleting this note will permanently remove its contents",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5E4E8C)
                )

                Spacer(Modifier.height(22.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(14.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A42C8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}
