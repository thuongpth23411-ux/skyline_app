package com.skyline.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    Call<GeminiResponse> generateResponse(@Query("key") String apiKey, @Body GeminiRequest request);
}
