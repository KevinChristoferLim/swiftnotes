package com.example.wifty.data.api

import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Header

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
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun signUp(@Body request: SignUpRequest): Response<LoginResponse>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<LoginResponse>

    @DELETE("/api/auth/delete-account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>
}
