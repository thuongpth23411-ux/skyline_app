package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityCardPaymentBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCardPaymentBinding binding = ActivityCardPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        double amount = getIntent().getDoubleExtra("totalAmount", 0);
        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvAmount.setText(df.format(amount) + " VND");

        String masked = getIntent().getStringExtra("card_masked");
        if (masked != null) {
            binding.tvCardMasked.setText(masked);
        }

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnContinue.setOnClickListener(v -> sendOtpAndNavigate(binding));
    }

    private void sendOtpAndNavigate(ActivityCardPaymentBinding binding) {
        binding.btnContinue.setEnabled(false);
        binding.btnContinue.setText("ĐANG GỬI MÃ...");

        String email = getIntent().getStringExtra("passenger_email");
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        RetrofitClient.getInstance().sendPaymentOtp(body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(CardPaymentActivity.this, PaymentOtpActivity.class);
                    if (getIntent().getExtras() != null) {
                        intent.putExtras(getIntent().getExtras());
                    }
                    intent.putExtra("otp_message", "Mã xác thực (OTP) đã được gửi đến email: " + email);
                    intent.putExtra("server_otp", response.body().getMessage());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CardPaymentActivity.this, "Lỗi gửi mã OTP", Toast.LENGTH_SHORT).show();
                    binding.btnContinue.setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(CardPaymentActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                binding.btnContinue.setEnabled(true);
            }
        });
    }
}
