package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;

public class ResetSuccessActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        setupHomeButton();
        findViewById(R.id.btnPrimary).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.tvHome).setOnClickListener(v -> goHome());
    }
}
