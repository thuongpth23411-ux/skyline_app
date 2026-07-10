package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityPaymentOtpBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentOtpActivity extends AppCompatActivity {

    private ActivityPaymentOtpBinding binding;
    private int retryCount = 3;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnVerify.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString();
            if ("123456".equals(otp)) {
                saveBookingToDatabase();
            } else {
                retryCount--;
                if (retryCount <= 0) {
                    navigateToFailure("Sai mã OTP quá 3 lần. Vui lòng thử lại sau 5 phút.");
                } else {
                    binding.tvError.setVisibility(View.VISIBLE);
                    String errorMsg = String.format(Locale.getDefault(), "Mã OTP không chính xác. Còn lại %d lần thử.", retryCount);
                    binding.tvError.setText(errorMsg);
                }
            }
        });

        startTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResend.setText(String.format(Locale.getDefault(), "Gửi lại mã (%ds)", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    navigateToFailure("Hết thời gian xác thực OTP. Vui lòng thử lại sau 5 phút.");
                }
            }
        }.start();
    }

    private void saveBookingToDatabase() {
        binding.btnVerify.setEnabled(false);
        binding.btnVerify.setText("ĐANG XÁC THỰC...");

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
        bookingData.put("totalAmount", total);
        bookingData.put("paymentMethod", intent.getStringExtra("payment_method"));
        
        List<Map<String, String>> flightsList = new ArrayList<>();
        
        // Flight 1 (Departure)
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.skyline.app.network.Flight flight = gson.fromJson(flightJson, com.skyline.app.network.Flight.class);
        Map<String, String> f1 = new HashMap<>();
        f1.put("flightId", flight.getId());
        f1.put("seatNumber", intent.getStringExtra("selected_seat"));
        f1.put("ticketType", returnFlightJson != null ? "Chiều đi" : "Một chiều");
        flightsList.add(f1);
        
        // Flight 2 (Return - if any)
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
                    String error = "Lỗi lưu thông tin vé";
                    if (response.body() != null && response.body().getMessage() != null) {
                        error = response.body().getMessage();
                    }
                    navigateToFailure(error);
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
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    }

    private void navigateToFailure(String message) {
        Intent intent = new Intent(this, PaymentFailureActivity.class);
        intent.putExtra("error_message", message);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
