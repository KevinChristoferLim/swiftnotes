package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.wifty.R

/**
 * Bottom toolbar composable
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

            // Image attachment icon (PNG)
            IconButton(onClick = onImageClick) {
                Icon(
                    painter = painterResource(R.drawable.imageicon),
                    contentDescription = "Attach Image",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onInsertChecklistClick) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Checklist",
                    tint = Color.White
                )
            }

            // File attachment icon (PNG)
            IconButton(onClick = onFileClick) {
                Icon(
                    painter = painterResource(R.drawable.fileicon),
                    contentDescription = "Attach File",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Floating More menu
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
        modifier = modifier.wrapContentSize()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(180.dp)
        ) {

            MenuItem(
                icon = rememberVectorPainter(Icons.Default.Delete),
                text = "Delete",
                onClick = onDeleteClick
            )

            // Copy icon (PNG)
            MenuItem(
                icon = painterResource(R.drawable.copyicon),
                text = "Copy",
                onClick = onCopyClick
            )

            MenuItem(
                icon = rememberVectorPainter(Icons.Default.Share),
                text = "Collaborators",
                onClick = onCollaboratorClick
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = text,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

/**
 * Delete confirmation dialog
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
                modifier = Modifier
                    .width(260.dp)
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF5A3EAA)
                        )
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(14.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7A42C8)
                        ),
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
