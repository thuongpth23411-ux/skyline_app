package com.skyline.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivitySupportBinding;

public class SupportActivity extends AppCompatActivity {

    private ActivitySupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClicks();
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCall.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:19001234"));
            startActivity(dialIntent);
        });

        binding.btnChat.setOnClickListener(v -> {
            startActivity(new Intent(SupportActivity.this, ChatActivity.class));
        });
    }
}
