package com.example.wifty.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wifty.ui.screens.AccountManagement.EditProfileScreen
import com.example.wifty.ui.screens.AccountManagement.ProfileScreen
import com.example.wifty.ui.screens.AccountManagement.OTP.ForgotPasswordScreen
import com.example.wifty.ui.screens.AccountManagement.OTP.OtpVerificationScreen
import com.example.wifty.ui.screens.AccountManagement.OTP.CreateNewPasswordScreen
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
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                // 👇 THIS IS THE NEW PART THAT MAKES IT WORK
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
        }

        // ---- Profile Screen ----
        composable(Routes.Profile.route) {
            ProfileScreen(
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
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
        }

        // ---- Edit Profile Screen ----
        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ================= OTP / PASSWORD FLOW =================

        // 1. Forgot Password (Enter Email)
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onSubmit = {
                    navController.navigate(Routes.OtpVerification.route)
                }
            )
        }

        // 2. OTP Verification (Enter Code)
        composable(Routes.OtpVerification.route) {
            OtpVerificationScreen(
                onBack = { navController.popBackStack() },
                onSubmit = {
                    navController.navigate(Routes.CreateNewPassword.route)
                }
            )
        }

        // 3. Create New Password (Reset & Success)
        composable(Routes.CreateNewPassword.route) {
            CreateNewPasswordScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.NotesList.route) { inclusive = true }
                    }
                }
            )
        }

        // =======================================================

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