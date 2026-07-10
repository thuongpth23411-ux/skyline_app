package com.skyline.app.network;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    @GET("promotions")
    Call<List<Promotion>> getPromotions();

    @POST("promotions/toggle-save")
    Call<BaseResponse> toggleSaveVoucher(@Header("Authorization") String token, @Body Map<String, String> body);

    @GET("promotions/my-vouchers")
    Call<List<Promotion>> getMyVouchers(@Header("Authorization") String token);

    @GET("airports")
    Call<List<Airport>> getAirports();

    @GET("airlines")
    Call<List<Airline>> getAirlines();

    @POST("flights/search")
    Call<List<Flight>> searchFlights(@Body FlightSearchRequest request);

    @GET("tickets/my-tickets")
    Call<List<TicketResponse>> getMyTickets(@Header("Authorization") String token);

    @POST("tickets/send-email")
    Call<BaseResponse> sendTicketEmail(@Header("Authorization") String token, @Body Map<String, Object> body);

    @POST("tickets/create")
    Call<BaseResponse> createBooking(@Body Map<String, Object> bookingData);

    @GET("flights/{flightId}/seats")
    Call<List<FlightSeat>> getFlightSeats(@Path("flightId") String flightId);

    @GET("blogs")
    Call<List<Blog>> getBlogs();

    @GET("blogs/featured")
    Call<List<Blog>> getFeaturedBlogs();

    @GET("blogs/{identifier}")
    Call<Blog> getBlogByIdentifier(@Path("identifier") String identifier);
}
