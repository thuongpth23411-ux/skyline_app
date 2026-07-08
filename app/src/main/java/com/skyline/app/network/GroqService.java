package com.skyline.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GroqService {
    @POST("openai/v1/chat/completions")
    Call<GroqResponse> generateChatResponse(
        @Header("Authorization") String authHeader,
        @Body GroqRequest request
    );
}
