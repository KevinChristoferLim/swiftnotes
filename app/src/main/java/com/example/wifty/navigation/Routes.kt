package com.example.wifty.navigation

sealed class Routes(val route: String) {

    object CoverView : Routes("cover_view")

    object Login : Routes("login")

    object Profile : Routes("profile")

    object EditProfile : Routes("edit_profile")

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

    // 👇 ADDED THESE FOR THE OTP FLOW
    object ForgotPassword : Routes("forgot_password")
    object OtpVerification : Routes("otp_verification")
    object CreateNewPassword : Routes("create_new_password")
}