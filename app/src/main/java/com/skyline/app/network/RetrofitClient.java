package com.skyline.app.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static ApiService instance;

    private static String getBaseUrl() {
        // Kiểm tra xem có phải máy ảo không
        boolean isEmulator = android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.FINGERPRINT.contains("vbox")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86");

        if (isEmulator) {
            return "http://10.0.2.2:3000/api/";
        } else {
            // TỰ ĐỘNG LẤY IP NẾU CÓ THỂ HOẶC DÙNG IP CỐ ĐỊNH
            // Bạn hãy kiểm tra IP máy tính (ipconfig) và sửa tại đây nếu cần
            return "http://192.168.1.189:3000/api/";
        }
    }

    public static ApiService getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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
