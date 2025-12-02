package com.example.wifty.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wifty.ui.screens.AccountManagement.ProfileScreen
import com.example.wifty.ui.screens.login.AuthScreen
import com.example.wifty.ui.screens.*
import com.example.wifty.ui.screens.notes.*
import com.example.wifty.viewmodel.FolderViewModel
import com.example.wifty.viewmodel.NotesViewModel

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

        composable(Routes.Login.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.NotesList.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Routes.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = { /* mock logout: navController.navigate(Routes.Login.route) */ },
                onDeleteConfirm = { /* handle mock delete */ }
            )
        }


        // ---- NOTES LIST ----
        composable(Routes.NotesList.route) {
            NotesListScreen(
                viewModel = notesVM,
                folderViewModel = folderVM,
                onCreateNewNote = {
                    navController.navigate(Routes.CreateNote.route)
                },
                onOpenNote = { id: String ->
                    navController.navigate(Routes.ViewNote.pass(id))
                },

                onOpenFolders = {
                    navController.navigate(Routes.FolderList.route)
                },
                onOpenProfile = {
                    navController.navigate(Routes.Profile.route)
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
                notesVM = notesVM,
                viewModel = folderVM,
                onCreateFolder = { navController.navigate(Routes.CreateFolder.route) },
                onOpenFolder = { folderId: String ->
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
                onBack = { navController.popBackStack() },
                onOpenNote = { noteId ->
                    navController.navigate(Routes.ViewNote.pass(noteId))
                }
            )
        }
    }
}