package com.example.wifty.data.api

import com.example.wifty.model.Folder
import com.example.wifty.model.Note
import com.example.wifty.model.Reminder
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserData?
)

data class UserData(
    val id: String,
    val username: String,
    val email: String,
    val profile_picture: String?
)

data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class CollaboratorRequest(
    val email: String
)

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun signUp(@Body request: SignUpRequest): Response<LoginResponse>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<LoginResponse>

    @DELETE("/api/auth/delete-account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>

    @Multipart
    @PUT("/api/auth/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part profilePicture: MultipartBody.Part? // Nullable if no image is uploaded
    ): Response<LoginResponse> // Assuming the response is similar to LoginResponse

    @PUT("/api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit> // Assuming the endpoint returns a simple success/fail response

    @GET("/api/notes")
    suspend fun getNotes(@Header("Authorization") token: String): Response<List<Note>>

    @POST("/api/notes")
    suspend fun createNote(
        @Header("Authorization") token: String,
        @Body note: Note
    ): Response<Note>

    @PUT("/api/notes/{noteId}")
    suspend fun updateNote(
        @Header("Authorization") token: String,
        @Path("noteId") noteId: String,
        @Body note: Note
    ): Response<Note>

    @DELETE("/api/notes/{noteId}")
    suspend fun deleteNote(
        @Header("Authorization") token: String,
        @Path("noteId") noteId: String
    ): Response<Unit>

    @POST("/api/notes/{noteId}/collaborators")
    suspend fun addCollaborator(
        @Header("Authorization") token: String,
        @Path("noteId") noteId: String,
        @Body collaboratorRequest: CollaboratorRequest
    ): Response<Unit>

    @GET("/api/folders")
    suspend fun getAllFolders(@Header("Authorization") token: String): Response<List<Folder>>

    @POST("/api/folders")
    suspend fun createFolder(@Header("Authorization") token: String, @Body folder: Folder): Response<Folder>

    @PUT("/api/folders/{folderId}")
    suspend fun updateFolder(
        @Header("Authorization") token: String,
        @Path("folderId") folderId: String,
        @Body folder: Folder
    ): Response<Folder>

    @DELETE("/api/folders/{folderId}")
    suspend fun deleteFolder(
        @Header("Authorization") token: String,
        @Path("folderId") folderId: String
    ): Response<Unit>

    @POST("/api/folders/{folderId}/notes/{noteId}")
    suspend fun addNoteToFolder(
        @Header("Authorization") token: String,
        @Path("folderId") folderId: String,
        @Path("noteId") noteId: String
    ): Response<Unit>

    @POST("/api/reminders")
    suspend fun createReminder(@Header("Authorization") token: String, @Body reminder: Reminder): Response<Reminder>

    @PUT("/api/reminders/{reminderId}")
    suspend fun updateReminder(
        @Header("Authorization") token: String,
        @Path("reminderId") reminderId: String,
        @Body reminder: Reminder
    ): Response<Reminder>

    @DELETE("/api/reminders/{reminderId}")
    suspend fun deleteReminder(
        @Header("Authorization") token: String,
        @Path("reminderId") reminderId: String
    ): Response<Unit>

    @GET("/api/reminders/{reminderId}")
    suspend fun getReminder(
        @Header("Authorization") token: String,
        @Path("reminderId") reminderId: String
    ): Response<Reminder>

    @GET("/api/notes/{noteId}/reminders")
    suspend fun getRemindersByNoteId(
        @Header("Authorization") token: String,
        @Path("noteId") noteId: String
    ): Response<List<Reminder>>

    @GET("/api/reminders")
    suspend fun getAllReminders(@Header("Authorization") token: String): Response<List<Reminder>>
}
