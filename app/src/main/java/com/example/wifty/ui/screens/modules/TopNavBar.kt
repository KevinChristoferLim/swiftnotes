package com.example.wifty.ui.screens.modules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wifty.R
import androidx.compose.ui.res.painterResource

/**
 * A reusable TopNavBar with back button, title, subtitle, optional profile icon,
 * and a search function that can be filtered by type ("Notes" or "Folders").
 */
@Composable
fun TopNavBar(
    showProfile: Boolean = false,
    onSearchClick: ((query: String, type: String) -> Unit)? = null, // type = "Notes" or "Folders"
    searchType: String = "Notes", // default type
    onOpenFolders: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Search icon
        onSearchClick?.let { searchCallback ->
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { showSearchDialog = true }
            )
            Spacer(modifier = Modifier.width(18.dp))
        }

        // Folders icon
        onOpenFolders?.let {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Folders",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { it() }
            )
            Spacer(modifier = Modifier.width(18.dp))
        }

        // Profile icon
        onOpenProfile?.let {
            Image(
                painter = painterResource(id = R.drawable.sample_profile),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { it() }
            )
        }
    }

    // ------------------------------
    // Search Dialog
    // ------------------------------
    if (showSearchDialog && onSearchClick != null) {
        Dialog(onDismissRequest = { showSearchDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Search $searchType", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Enter search term") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            showSearchDialog = false
                            onSearchClick(searchQuery, searchType)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Search")
                    }
                }
            }
        }
    }
}
