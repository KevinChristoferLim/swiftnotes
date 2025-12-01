package com.example.wifty.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wifty.ui.screens.*
import com.example.wifty.ui.screens.notes.*
import com.example.wifty.viewmodel.NotesViewModel
import com.example.wifty.viewmodel.FolderViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    notesVM: NotesViewModel,
    folderVM: FolderViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Routes.CoverView.route
    ) {

        // ---- Cover Screen ----
        composable(Routes.CoverView.route) {
            CoverViewScreen(
                onContinue = {
                    navController.navigate(Routes.NotesList.route) {
                        popUpTo(Routes.CoverView.route) { inclusive = true }
                    }
                }
            )
        }

        // ---- NOTES LIST ----
        composable(Routes.NotesList.route) {
            NotesListScreen(
                viewModel = notesVM,
                onCreateNewNote = {
                    navController.navigate(Routes.CreateNote.route)
                },
                onOpenNote = { id ->
                    navController.navigate(Routes.ViewNote.pass(id))
                },
                onOpenFolders = {
                    navController.navigate(Routes.FolderList.route)
                }
            )
        }

        // ---- CREATE NOTE ----
        composable(Routes.CreateNote.route) {
            CreateNoteScreen(
                viewModel = notesVM,
                onCreated = { newId ->
                    navController.navigate(Routes.ViewNote.pass(newId)) {
                        popUpTo(Routes.NotesList.route)
                    }
                }
            )
        }

        // ---- VIEW NOTE ----
        composable(Routes.ViewNote.route) { backStack ->
            val id = backStack.arguments?.getString("noteId") ?: ""
            ViewNoteScreen(
                noteId = id,
                viewModel = notesVM,
                onClose = { navController.popBackStack() }
            )
        }

        // ---- FOLDER LIST ----
        composable(Routes.FolderList.route) {
            FolderListScreen(
                viewModel = folderVM,
                onCreateFolder = { navController.navigate(Routes.CreateFolder.route) },
                onOpenFolder = { folderId ->
                    navController.navigate(Routes.ViewFolder.pass(folderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ---- CREATE FOLDER ----
        composable(Routes.CreateFolder.route) {
            AddFolderScreen(
                folderViewModel = folderVM,
                onSaved = { navController.popBackStack() }
            )
        }

        // ---- VIEW FOLDER ----
        composable(Routes.ViewFolder.route) { backStack ->
            val id = backStack.arguments?.getString("folderId") ?: ""
            ViewFolderScreen(
                folderId = id,
                folderVM = folderVM,
                notesVM = notesVM,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
