package com.skyline.app.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static ApiService instance;

    private static String getBaseUrl() {
        return "http://127.0.0.1:3000/api/";
    }

    public static ApiService getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true) // Tự động thử lại nếu kết nối lỗi
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                        .addHeader("Connection", "close") // Đóng kết nối sau mỗi request để tránh stale
                        .build();
                    return chain.proceed(request);
                })
                .build();

            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

            instance = retrofit.create(ApiService.class);
        }
        return instance;
    }
}
