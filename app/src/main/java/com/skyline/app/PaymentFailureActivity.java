package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityPaymentFailureBinding;

public class PaymentFailureActivity extends AppCompatActivity {
    private ActivityPaymentFailureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentFailureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String errorMsg = getIntent().getStringExtra("error_message");
        if (errorMsg != null) {
            binding.tvErrorMsg.setText(errorMsg);
        }

        binding.btnRetry.setOnClickListener(v -> finish());
        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
