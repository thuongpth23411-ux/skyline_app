package com.skyline.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/send-otp-reg")
    Call<BaseResponse> sendOtpReg(@Body ForgotPasswordRequest request);

    @POST("auth/verify-otp")
    Call<BaseResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/register-finalize")
    Call<AuthResponse> registerFinalize(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<BaseResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Call<BaseResponse> resetPassword(@Body ResetPasswordRequest request);

    @retrofit2.http.GET("airports")
    Call<java.util.List<Airport>> getAirports();

    @POST("flights/search")
    Call<java.util.List<Flight>> searchFlights(@Body FlightSearchRequest request);
}
