package com.example.wifty.data.api

import com.example.wifty.model.Folder
import com.example.wifty.model.Note
import com.example.wifty.model.Reminder
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.PUT
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

interface ApiService {
    // Auth
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun signUp(@Body request: SignUpRequest): Response<LoginResponse>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<LoginResponse>

    @DELETE("/api/auth/delete-account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>

    // Notes
    @GET("/api/notes")
    suspend fun getAllNotes(@Header("Authorization") token: String): Response<List<Note>>

    @GET("/api/notes/{id}")
    suspend fun getNote(@Header("Authorization") token: String, @Path("id") id: String): Response<Note>

    @POST("/api/notes")
    suspend fun createNote(@Header("Authorization") token: String, @Body note: Note): Response<Note>

    @PUT("/api/notes/{id}")
    suspend fun updateNote(@Header("Authorization") token: String, @Path("id") id: String, @Body note: Note): Response<Note>

    @DELETE("/api/notes/{id}")
    suspend fun deleteNote(@Header("Authorization") token: String, @Path("id") id: String): Response<Unit>

    // Folders
    @GET("/api/folders")
    suspend fun getAllFolders(@Header("Authorization") token: String): Response<List<Folder>>

    @POST("/api/folders")
    suspend fun createFolder(@Header("Authorization") token: String, @Body folder: Folder): Response<Folder>

    @PUT("/api/folders/{id}")
    suspend fun updateFolder(@Header("Authorization") token: String, @Path("id") id: String, @Body folder: Folder): Response<Folder>

    @DELETE("/api/folders/{id}")
    suspend fun deleteFolder(@Header("Authorization") token: String, @Path("id") id: String): Response<Unit>

    // Reminders
    @GET("/api/reminders")
    suspend fun getAllReminders(@Header("Authorization") token: String): Response<List<Reminder>>

    @GET("/api/reminders/note/{noteId}")
    suspend fun getRemindersByNoteId(@Header("Authorization") token: String, @Path("noteId") noteId: String): Response<List<Reminder>>

    @GET("/api/reminders/{id}")
    suspend fun getReminder(@Header("Authorization") token: String, @Path("id") id: String): Response<Reminder>

    @POST("/api/reminders")
    suspend fun createReminder(@Header("Authorization") token: String, @Body reminder: Reminder): Response<Reminder>

    @PUT("/api/reminders/{id}")
    suspend fun updateReminder(@Header("Authorization") token: String, @Path("id") id: String, @Body reminder: Reminder): Response<Reminder>

    @DELETE("/api/reminders/{id}")
    suspend fun deleteReminder(@Header("Authorization") token: String, @Path("id") id: String): Response<Unit>
}
