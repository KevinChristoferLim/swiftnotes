package com.example.wifty.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wifty.ui.screens.*
import com.example.wifty.viewmodel.NotesViewModel

@Composable
fun AppNavGraph(navController: NavHostController, notesVM: NotesViewModel) {

    NavHost(
        navController = navController,
        startDestination = Routes.NotesList.route
    ) {

        composable(Routes.NotesList.route) {
            NotesListScreen(
                viewModel = notesVM,
                onCreateNewNote = {
                    navController.navigate(Routes.CreateNote.route)
                },
                onOpenNote = { id ->
                    navController.navigate(Routes.ViewNote.pass(id))
                }
            )
        }

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

        composable(Routes.ViewNote.route) { backStack ->
            val id = backStack.arguments?.getString("noteId") ?: ""

            ViewNoteScreen(
                noteId = id,
                viewModel = notesVM,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
