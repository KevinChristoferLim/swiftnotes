package com.example.wifty.ui.screens.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wifty.ui.screens.modules.TopNavBar
import com.example.wifty.R

@Composable
fun LandingScreen(
    onCreateNewNote: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFFEDE9FF), Color.White))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp) // top padding for TopNavBar
        ) {

            // --- Top Nav Bar ---
            TopNavBar(
                onSearchClick = { /* TODO: add search */ },
                onOpenFolders = onOpenFolders,
                onOpenProfile = onOpenProfile
            )

            // --- Landing Content ---
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // --- Main Illustration ---
                    Image(
                        painter = painterResource(id = R.drawable.mainpageillust),
                        contentDescription = "Main Page Illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp), // adjust height as needed
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Start Your Journey",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Every big step starts with a small step.\nNote your first idea and start your journey!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FloatingActionButton(onClick = onCreateNewNote) {
                        Text("+")
                    }
                }
            }
        }
    }
}
