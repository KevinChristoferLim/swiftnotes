package com.example.wifty

import com.example.wifty.ui.screens.LandingScreen
import com.example.wifty.ui.screens.AddFolderScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wifty.ui.theme.WiftyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WiftyTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "landing"
                ) {
                    composable("landing") {
                        LandingScreen(
                            onAddClick = {
                                navController.navigate("add_folder")
                            }
                        )
                    }

                    composable("add_folder") {
                        AddFolderScreen()
                    }
                }
            }
        }
    }
}
