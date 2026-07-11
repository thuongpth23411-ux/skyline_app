package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.skyline.app.databinding.ActivityCardPaymentBinding;

public class CardPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCardPaymentBinding binding = ActivityCardPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentOtpActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            finish();
        });
    }
}
