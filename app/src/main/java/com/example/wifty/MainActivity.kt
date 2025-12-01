package com.example.wifty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.wifty.navigation.AppNavGraph
import com.example.wifty.ui.theme.WiftyTheme
import com.example.wifty.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WiftyTheme {
                val navController = rememberNavController()

                // Create your NotesViewModel here
                val notesVM: NotesViewModel = viewModel()
                val folderVM: FolderViewModel = viewModel()


                // Pass it to the navigation graph
                AppNavGraph(
                    navController = navController,
                    notesVM = notesVM,
                    folderVM = folderVM
                )
            }
        }
    }
}
