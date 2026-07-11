package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityMomoPaymentBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomoPaymentActivity extends AppCompatActivity {

    private ActivityMomoPaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMomoPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        double amount = getIntent().getDoubleExtra("totalAmount", 0);
        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvAmount.setText(String.format("%sđ", df.format(amount)));

        String passenger = getIntent().getStringExtra("passenger_name");
        binding.tvPayerName.setText(passenger != null ? passenger.toUpperCase() : "---");
        
        binding.tvTransactionId.setText(String.valueOf(System.currentTimeMillis() / 1000));
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        binding.tvTime.setText(sdf.format(new java.util.Date()));

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnPayMomo.setOnClickListener(v -> sendOtpAndNavigate());
    }

    private void sendOtpAndNavigate() {
        binding.btnPayMomo.setEnabled(false);
        binding.btnPayMomo.setText("ĐANG XỬ LÝ...");

        String email = getIntent().getStringExtra("passenger_email");
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        RetrofitClient.getInstance().sendPaymentOtp(body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(MomoPaymentActivity.this, PaymentOtpActivity.class);
                    if (getIntent().getExtras() != null) {
                        intent.putExtras(getIntent().getExtras());
                    }
                    intent.putExtra("otp_message", "Mã xác thực giao dịch MoMo đã được gửi đến email: " + email);
                    // Pass the real OTP if we want to verify it in the next screen
                    // In a real app, the verification happens on the server.
                    // For this simulation, we'll use the one from server response.
                    intent.putExtra("server_otp", response.body().getMessage()); // Message contains OTP in my backend code
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MomoPaymentActivity.this, "Không thể gửi OTP. Thử lại sau.", Toast.LENGTH_SHORT).show();
                    binding.btnPayMomo.setEnabled(true);
                    binding.btnPayMomo.setText("XÁC NHẬN THANH TOÁN");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(MomoPaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnPayMomo.setEnabled(true);
                binding.btnPayMomo.setText("XÁC NHẬN THANH TOÁN");
            }
        });
    }
}
