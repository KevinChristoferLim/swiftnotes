package com.example.wifty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wifty.viewmodel.FolderViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    viewModel: FolderViewModel,
    onCreateFolder: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onBack: () -> Unit
) {
    val folders by viewModel.folders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("All Folders", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Subfolders", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCreateFolder) {
                        Icon(Icons.Default.Add, contentDescription = "Create Folder")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(folders) { folder ->
                FolderCard(
                    name = folder.title,
                    hashtag = folder.tag,
                    notesCount = 0, // You do not store notes inside folder yet
                    gradientColors = listOf(
                        Color(folder.colorLong),
                        Color(folder.colorLong).copy(alpha = 0.7f)
                    ),
                    onClick = { onOpenFolder(folder.id) }
                )
            }
        }
    }
}

@Composable
fun FolderCard(
    name: String,
    hashtag: String,
    notesCount: Int,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                brush = Brush.verticalGradient(colors = gradientColors),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(hashtag, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
        }

        Text(
            text = "$notesCount notes",
            modifier = Modifier.align(Alignment.BottomEnd),
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Generates a predictable gradient color per folder.
 * You can customize it or store color in Folder entity.
 */
fun generateGradient(seed: String): List<Color> {
    val index = (seed.hashCode().absoluteValue % 4)

    return when (index) {
        0 -> listOf(Color(0xFF74EBD5), Color(0xFFACB6E5))   // blue-purple
        1 -> listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))   // red-pink
        2 -> listOf(Color(0xFFAAF683), Color(0xFF00CDAC))   // green-teal
        else -> listOf(Color(0xFFFFF6B7), Color(0xFFF6416C)) // yellow-red
    }
}

private val Int.absoluteValue: Int
    get() = if (this < 0) -this else this
