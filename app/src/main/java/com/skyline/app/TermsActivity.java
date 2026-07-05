package com.skyline.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityTermsBinding;

public class TermsActivity extends AppCompatActivity {

    private ActivityTermsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSupport.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(TermsActivity.this, SupportActivity.class);
            startActivity(intent);
        });
    }
}
