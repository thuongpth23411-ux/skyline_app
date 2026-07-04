package com.skyline.app;

import android.os.Bundle;

public class AccountSuccessActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_success);
        setupHomeButton();
        findViewById(R.id.btnExplore).setOnClickListener(v -> goHome());
    }
}
