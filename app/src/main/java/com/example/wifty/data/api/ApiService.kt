package com.example.wifty.data.api

import com.example.wifty.model.Note
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

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
    @SerializedName("profile_picture") val profile_picture: String?
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

data class CreateNoteRequest(
    val title: String,
    @SerializedName("description") val content: String?,
    @SerializedName("folder_id") val folderId: Int? = null,
    @SerializedName("reminder_date_millis") val reminderDateMillis: Long? = null,
    @SerializedName("reminder_time_millis") val reminderTimeMillis: Long? = null,
    @SerializedName("reminder_repeat") val reminderRepeat: List<String>? = null,
    @SerializedName("reminder_location") val reminderLocation: String? = null
)

data class PinRequest(
    val pin: String
)

data class UpdateNoteRequest(
    val title: String?,
    @SerializedName("description") val content: String?,
    @SerializedName("folder_id") val folderId: Int? = null,
    @SerializedName("reminder_date_millis") val reminderDateMillis: Long? = null,
    @SerializedName("reminder_time_millis") val reminderTimeMillis: Long? = null,
    @SerializedName("reminder_repeat") val reminderRepeat: List<String>? = null,
    @SerializedName("reminder_location") val reminderLocation: String? = null
)

data class AddCollaboratorRequest(
    val email: String,
    val role: String? = "editor"
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

    @PUT("/api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @Multipart
    @PUT("/api/auth/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part profilePicture: MultipartBody.Part?
    ): Response<LoginResponse>

    @GET("/api/notes")
    suspend fun getNotes(@Header("Authorization") auth: String): Response<List<Note>>

    @POST("/api/notes")
    suspend fun createNote(
        @Header("Authorization") auth: String,
        @Body body: CreateNoteRequest
    ): Response<Map<String, Any>>

    @PUT("/api/notes/{id}")
    suspend fun updateNote(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: UpdateNoteRequest
    ): Response<Map<String, Any>>

    @DELETE("/api/notes/{id}")
    suspend fun deleteNote(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): Response<Map<String, Any>>

    @POST("/api/notes/{noteId}/collaborators")
    suspend fun addCollaborator(
        @Header("Authorization") auth: String,
        @Path("noteId") noteId: String,
        @Body body: AddCollaboratorRequest
    ): Response<Map<String, Any>>

    @DELETE("/api/notes/{noteId}/collaborators/{collaboratorId}")
    suspend fun removeCollaborator(
        @Header("Authorization") auth: String,
        @Path("noteId") noteId: String,
        @Path("collaboratorId") collaboratorId: String
    ): Response<Map<String, Any>>

    @POST("/api/notes/{id}/lock")
    suspend fun lockNote(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: PinRequest
    ): Response<Map<String, Any>>

    @POST("/api/notes/{id}/unlock")
    suspend fun unlockNote(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: PinRequest
    ): Response<Map<String, Any>>

    @POST("/api/notes/{id}/view")
    suspend fun viewLockedNote(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Body body: PinRequest
    ): Response<Map<String, Any>>
}
