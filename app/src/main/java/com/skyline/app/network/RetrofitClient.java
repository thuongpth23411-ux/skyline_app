package com.skyline.app.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static ApiService instance;
    private static GroqService groqInstance;

    private static String getBaseUrl() {
        // 127.0.0.1 (localhost) hoạt động tốt cho cả Emulator và Máy thật (nếu dùng adb reverse)
        return "http://127.0.0.1:3000/api/";
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = createRetrofit(getBaseUrl()).create(ApiService.class);
        }
        return instance;
    }

    public static GroqService getGroqInstance() {
        if (groqInstance == null) {
            groqInstance = createRetrofit("https://api.groq.com/")
                    .create(GroqService.class);
        }
        return groqInstance;
    }

    private static Retrofit createRetrofit(String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();
    }
}
