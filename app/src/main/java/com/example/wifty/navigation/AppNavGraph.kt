package com.example.wifty.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wifty.data.api.RetrofitClient
import com.example.wifty.ui.screens.AccountManagement.ChangePasswordScreen
import com.example.wifty.ui.screens.AccountManagement.EditProfileScreen
import com.example.wifty.ui.screens.AccountManagement.ProfileScreen
import com.example.wifty.ui.screens.login.AuthScreen
import com.example.wifty.ui.screens.login.AuthViewModel
import com.example.wifty.ui.screens.login.AuthViewModelFactory
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
    val application = LocalContext.current.applicationContext as Application
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(RetrofitClient.apiService, application))

    NavHost(
        navController = navController,
        startDestination = Routes.CoverView.route
    ) {

        // ---- Cover Screen ----
        composable(Routes.CoverView.route) {
            CoverViewScreen(
                onContinue = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.CoverView.route) { inclusive = true }
                    }
                }
            )
        }

        // ---- Home Screen ----
        composable(Routes.Home.route) {
            HomeScreen(
                notesVM = notesVM,
                folderVM = folderVM,
                onCreateNewNote = { navController.navigate(Routes.CreateNote.route) },
                onOpenNote = { id -> navController.navigate(Routes.ViewNote.pass(id)) },
                onOpenFolders = { navController.navigate(Routes.FolderList.route) },
                onOpenProfile = { navController.navigate(Routes.Profile.route) }
            )
        }


        // ---- Login Screen ----
        composable(Routes.Login.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
        }

        // ---- Profile Screen ----
        composable(Routes.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToEditProfile = {
                    navController.navigate(Routes.EditProfile.route)
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.NotesList.route) { inclusive = true }
                    }
                },
                onNavigateToChangePassword = {
                    navController.navigate(Routes.ChangePassword.route)
                },
                onDeleteAccount = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ---- Edit Profile Screen ----
        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Change Password Screen ----
        composable(Routes.ChangePassword.route) {
            ChangePasswordScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onPasswordChanged = { navController.popBackStack() }
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
                        popUpTo(Routes.CreateNote.route) { inclusive = true
                        }
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
                onOpenFolder = { folderId ->
                    navController.navigate(Routes.ViewFolder.pass(folderId))
                },
                onOpenNote = { noteId ->
                    navController.navigate(Routes.ViewNote.pass(noteId))
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
                onOpenNote = { noteId -> navController.navigate(Routes.ViewNote.pass(noteId)) },
                onOpenProfile = { navController.navigate(Routes.Profile.route) } // <--- pass profile lambda
            )
        }
    }
}