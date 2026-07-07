package com.skyline.app.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

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

    @GET("auth/profile")
    Call<User> getProfile(@Header("Authorization") String token);

    @GET("auth/rank-benefits")
    Call<List<RankBenefit>> getRankBenefits(@Query("rank") String rank);

    @GET("airports")
    Call<List<Airport>> getAirports();

    @POST("flights/search")
    Call<List<Flight>> searchFlights(@Body FlightSearchRequest request);

    @GET("tickets/my-tickets")
    Call<List<TicketResponse>> getMyTickets(@Header("Authorization") String token);
}
