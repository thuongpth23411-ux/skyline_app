package com.skyline.app.network

data class BaseResponse(
    val success: Boolean,
    val message: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class User(
    val id: String,
    val email: String,
    val name: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val phone: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)
