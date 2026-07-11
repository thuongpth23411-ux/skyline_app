package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityPaymentFailureBinding;
import java.util.Locale;

public class PaymentFailureActivity extends AppCompatActivity {

    private ActivityPaymentFailureBinding binding;
    private CountDownTimer retryTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentFailureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String reason = getIntent().getStringExtra("error_message");
        if (reason != null) {
            binding.tvErrorReason.setText(reason);
        }

        binding.btnTryAgain.setOnClickListener(v -> {
            // Return to payment confirmation or start over
            finish();
        });

        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        startRetryTimer();
    }

    private void startRetryTimer() {
        retryTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                binding.tvRetryTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.tvRetryTimer.setText("00:00");
                binding.layoutRetryInfo.setVisibility(View.GONE);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (retryTimer != null) retryTimer.cancel();
    }
}
