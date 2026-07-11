package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityPaymentFailureBinding;

import java.util.Locale;

public class PaymentFailureActivity extends AppCompatActivity {

    private ActivityPaymentFailureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentFailureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String error = getIntent().getStringExtra("error_message");
        if (error != null) {
            binding.tvErrorMessage.setText(error);
            
            if (error.contains("5 phút")) {
                startRetryTimer();
            }
        }

        binding.btnRetry.setOnClickListener(v -> finish());
        
        binding.btnChangeMethod.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfirmPaymentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void startRetryTimer() {
        binding.btnRetry.setEnabled(false);
        new android.os.CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                binding.btnRetry.setText(getString(R.string.retry_after_time, timeStr));
            }

            @Override
            public void onFinish() {
                binding.btnRetry.setEnabled(true);
                binding.btnRetry.setText(R.string.retry_now);
            }
        }.start();
    }
}
