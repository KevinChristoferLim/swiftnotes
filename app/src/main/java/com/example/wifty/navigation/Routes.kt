package com.example.wifty.navigation

sealed class Routes(val route: String) {

    object CoverView : Routes("CoverView")

    object Landing : Routes("landing")

    object NotesList : Routes("notes_list")

    object CreateNote : Routes("create_note")

    object ViewNote : Routes("view_note/{noteId}") {
        fun pass(noteId: String) = "view_note/$noteId"
    }
}
