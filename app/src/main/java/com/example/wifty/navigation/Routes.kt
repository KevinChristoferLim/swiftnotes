package com.example.wifty.navigation

sealed class Routes(val route: String) {

    object CoverView : Routes("cover_view")

    object Profile : Routes("profile")

    object Login : Routes("login")

    object NotesList : Routes("notes_list")
    object CreateNote : Routes("create_note")

    object ViewNote : Routes("view_note/{noteId}") {
        fun pass(noteId: String) = "view_note/$noteId"
    }

    object FolderList : Routes("folder_list")
    object CreateFolder : Routes("create_folder")

    object ViewFolder : Routes("view_folder/{folderId}") {
        fun pass(folderId: String) = "view_folder/$folderId"
    }
}
