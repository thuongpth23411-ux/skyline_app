package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityVietqrPaymentBinding;

import java.text.DecimalFormat;
import java.util.Locale;

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

public class VietQRPaymentActivity extends AppCompatActivity {

    private ActivityVietqrPaymentBinding binding;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVietqrPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        double amount = getIntent().getDoubleExtra("totalAmount", 0);
        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvAmount.setText(df.format(amount) + " VND");
        
        binding.tvDescription.setText("SKYLINE" + System.currentTimeMillis() / 100000);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmPaid.setOnClickListener(v -> {
            saveBookingToDatabase();
        });

        startTimer();
    }

    private void saveBookingToDatabase() {
        binding.btnConfirmPaid.setEnabled(false);
        binding.btnConfirmPaid.setText("ĐANG XÁC THỰC...");

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
        bookingData.put("paymentMethod", "VietQR");
        
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

    private void startTimer() {
        timer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                binding.tvTimer.setText(String.format(Locale.getDefault(), "Hết hạn sau %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.tvTimer.setText("Đã hết thời gian thanh toán");
                navigateToFailure("Đã quá thời gian thực hiện giao dịch (15 phút).");
            }
        }.start();
    }

    private void navigateToSuccess() {
        if (timer != null) timer.cancel();
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        startActivity(intent);
        finish();
    }

    private void navigateToFailure(String message) {
        if (timer != null) timer.cancel();
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
