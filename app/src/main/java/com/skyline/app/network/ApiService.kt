package com.skyline.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/send-otp-reg")
    suspend fun sendOtpReg(@Body request: ForgotPasswordRequest): Response<BaseResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<BaseResponse>

    @POST("auth/register-finalize")
    suspend fun registerFinalize(@Body request: RegisterRequest): Response<BaseResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<BaseResponse>
}
