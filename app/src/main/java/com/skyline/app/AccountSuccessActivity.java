package com.skyline.app;

import android.os.Bundle;

public class AccountSuccessActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_success);
        setupHomeButton();
        findViewById(R.id.btnExplore).setOnClickListener(v -> {
            com.skyline.app.utils.NotificationHelper.showDropDownNotification(
                this,
                "welcome_reg",
                "Chào mừng thành viên mới",
                "Chào mừng bạn đã gia nhập gia đình Skyline! Hãy hoàn thiện hồ sơ để nhận thêm nhiều ưu đãi và dặm thưởng hấp dẫn ngay hôm nay.",
                com.skyline.app.utils.NotificationHelper.NotifType.PROFILE,
                null
            );
            goHome();
        });
    }
}
