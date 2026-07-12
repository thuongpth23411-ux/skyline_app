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

        String customMsg = getIntent().getStringExtra("otp_message");
        if (customMsg != null) {
            binding.tvMessage.setText(customMsg);
        }

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnVerify.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString();
            String serverOtp = getIntent().getStringExtra("server_otp");
            
            // If serverOtp is present (from new real OTP flow), verify against it.
            // Otherwise fallback to simulation code 123456
            boolean isValid = (serverOtp != null) ? serverOtp.equals(otp) : "123456".equals(otp);

            if (isValid) {
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
        
        List<String> names = intent.getStringArrayListExtra("passenger_names");
        List<String> seats = intent.getStringArrayListExtra("selectedSeats");
        List<String> returnSeats = intent.getStringArrayListExtra("returnSelectedSeats");
        
        com.google.gson.Gson gson = new com.google.gson.Gson();
        
        try {
            int totalPax = names != null ? names.size() : 1;
            final int[] successCount = {0};
            final int totalRequests = totalPax * (returnFlightJson != null ? 2 : 1);
            final boolean[] hasFailed = {false};

            for (int i = 0; i < totalPax; i++) {
                String pName = (names != null && i < names.size()) ? names.get(i) : (name != null ? name : "Khách hàng");
                
                // Flight 1 (Departure)
                if (flightJson != null && !flightJson.isEmpty()) {
                    String seat = (seats != null && i < seats.size()) ? seats.get(i) : "N/A";
                    createSingleTicket(userId, pName, intent.getStringExtra("passenger_email"), 
                        total / totalRequests, intent.getStringExtra("payment_method"),
                        flightJson, seat,
                        returnFlightJson != null ? "Khứ hồi - Đi" : "Một chiều",
                        successCount, totalRequests, hasFailed);
                }
                
                // Flight 2 (Return)
                if (returnFlightJson != null && !returnFlightJson.isEmpty()) {
                    String returnSeat = (returnSeats != null && i < returnSeats.size()) ? returnSeats.get(i) : "N/A";
                    createSingleTicket(userId, pName, intent.getStringExtra("passenger_email"), 
                        total / totalRequests, intent.getStringExtra("payment_method"),
                        returnFlightJson, returnSeat,
                        "Khứ hồi - Về",
                        successCount, totalRequests, hasFailed);
                }
            }
        } catch (Exception e) {
            navigateToFailure("Lỗi xử lý: " + e.getMessage());
        }
    }

    private void createSingleTicket(String userId, String pName, String email, double amount, String method,
                                   String flightJson, String seat, String type, 
                                   int[] successCount, int totalRequests, boolean[] hasFailed) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId != null ? userId : "guest_" + System.currentTimeMillis());
        data.put("passengerName", pName);
        data.put("email", email);
        data.put("totalAmount", amount);
        data.put("paymentMethod", method != null ? method : "Unknown");
        
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.skyline.app.network.Flight flight = gson.fromJson(flightJson, com.skyline.app.network.Flight.class);
        
        List<Map<String, String>> flightsList = new ArrayList<>();
        Map<String, String> f = new HashMap<>();
        f.put("flightId", flight.getId());
        f.put("seatNumber", seat != null ? seat : "N/A");
        f.put("ticketType", type);
        flightsList.add(f);
        data.put("flights", flightsList);

        RetrofitClient.getInstance().createBooking(data).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (hasFailed[0]) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    successCount[0]++;
                    if (successCount[0] == totalRequests) {
                        navigateToSuccess();
                    }
                } else {
                    hasFailed[0] = true;
                    navigateToFailure("Lỗi khi tạo vé cho " + pName);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                if (hasFailed[0]) return;
                hasFailed[0] = true;
                navigateToFailure("Lỗi mạng khi tạo vé cho " + pName);
            }
        });
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        // Ensure flight_json and other data are passed for "View My Ticket" button
        intent.putExtra("flight_json", getIntent().getStringExtra("flight_json"));
        intent.putExtra("selected_seat", getIntent().getStringExtra("selected_seat"));
        intent.putExtra("passenger_name", getIntent().getStringExtra("passenger_name"));
        intent.putExtra("totalAmount", getIntent().getDoubleExtra("totalAmount", 0));

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
