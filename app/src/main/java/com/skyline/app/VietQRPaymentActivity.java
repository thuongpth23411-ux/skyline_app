package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.skyline.app.databinding.ActivityVietqrPaymentBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
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
        
        String orderId = "SKYLINE" + (System.currentTimeMillis() % 1000000);
        binding.tvDescription.setText(orderId);

        // Load random/real QR using public VietQR-like API or just random data
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + orderId;
        Glide.with(this).load(qrUrl).into(binding.ivQR);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmPaid.setOnClickListener(v -> sendOtpAndNavigate());

        startTimer();
    }

    private void sendOtpAndNavigate() {
        binding.btnConfirmPaid.setEnabled(false);
        binding.btnConfirmPaid.setText("ĐANG KIỂM TRA...");

        String email = getIntent().getStringExtra("passenger_email");
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        RetrofitClient.getInstance().sendPaymentOtp(body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(VietQRPaymentActivity.this, PaymentOtpActivity.class);
                    if (getIntent().getExtras() != null) {
                        intent.putExtras(getIntent().getExtras());
                    }
                    intent.putExtra("otp_message", "Mã xác thực giao dịch VietQR đã được gửi đến email: " + email);
                    intent.putExtra("server_otp", response.body().getMessage());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(VietQRPaymentActivity.this, "Không thể xác thực. Thử lại sau.", Toast.LENGTH_SHORT).show();
                    binding.btnConfirmPaid.setEnabled(true);
                    binding.btnConfirmPaid.setText("XÁC NHẬN THANH TOÁN THÀNH CÔNG");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(VietQRPaymentActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnConfirmPaid.setEnabled(true);
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
