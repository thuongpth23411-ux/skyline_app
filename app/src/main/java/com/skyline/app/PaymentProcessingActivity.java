package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityPaymentProcessingBinding;

import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentProcessingActivity extends AppCompatActivity {

    private ActivityPaymentProcessingBinding binding;
    private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentProcessingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        method = getIntent().getStringExtra("payment_method");
        
        binding.tvMethodName.setText(method != null ? method.toUpperCase() : "THANH TOÁN");

        // Simulate network processing
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding != null) {
                binding.btnSimulateSuccess.setOnClickListener(v -> saveBookingToDatabase());
                binding.btnSimulateFailure.setOnClickListener(v -> navigateToFailure("Giao dịch bị từ chối bởi nhà cung cấp dịch vụ."));
            }
        }, 1500);
        
        binding.btnSimulateSuccess.setOnClickListener(v -> saveBookingToDatabase());
        binding.btnSimulateFailure.setOnClickListener(v -> navigateToFailure("Giao dịch bị từ chối bởi nhà cung cấp dịch vụ."));
    }

    private void saveBookingToDatabase() {
        binding.btnSimulateSuccess.setEnabled(false);
        binding.btnSimulateSuccess.setText("ĐANG XÁC THỰC...");

        SessionManager sm = new SessionManager(this);
        String userId = sm.getUserId();
        
        Intent intent = getIntent();
        double total = intent.getDoubleExtra("totalAmount", 0);
        String name = intent.getStringExtra("passenger_name");
        String flightJson = intent.getStringExtra("flight_json");
        String returnFlightJson = intent.getStringExtra("return_flight_json");
        
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("userId", userId != null ? userId : "guest_" + System.currentTimeMillis());
        bookingData.put("passengerName", name);
        bookingData.put("email", intent.getStringExtra("passenger_email")); // Gửi email để nhận vé/tạo tài khoản
        bookingData.put("totalAmount", total);
        bookingData.put("paymentMethod", method);
        
        List<Map<String, String>> flightsList = new ArrayList<>();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        
        // Flight 1
        com.skyline.app.network.Flight flight = gson.fromJson(flightJson, com.skyline.app.network.Flight.class);
        Map<String, String> f1 = new HashMap<>();
        f1.put("flightId", flight.getId());
        f1.put("seatNumber", intent.getStringExtra("selected_seat"));
        f1.put("ticketType", returnFlightJson != null ? "Chiều đi" : "Một chiều");
        flightsList.add(f1);
        
        // Flight 2
        if (returnFlightJson != null) {
            com.skyline.app.network.Flight returnFlight = gson.fromJson(returnFlightJson, com.skyline.app.network.Flight.class);
            Map<String, String> f2 = new HashMap<>();
            f2.put("flightId", returnFlight.getId());
            f2.put("seatNumber", intent.getStringExtra("return_selected_seat"));
            f2.put("ticketType", "Chiều về");
            flightsList.add(f2);
        }
        
        bookingData.put("flights", flightsList);

        RetrofitClient.getInstance().createBooking(bookingData).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    navigateToSuccess();
                } else {
                    String errorMessage = "Lỗi lưu thông tin vé";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    navigateToFailure(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                navigateToFailure("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        startActivity(intent);
        finish();
    }

    private void navigateToFailure(String message) {
        Intent intent = new Intent(this, PaymentFailureActivity.class);
        intent.putExtra("error_message", message);
        startActivity(intent);
        finish();
    }
}
