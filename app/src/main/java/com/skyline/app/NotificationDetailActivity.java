package com.skyline.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityNotificationDetailBinding;
import com.skyline.app.utils.NotificationHelper;
import com.skyline.model.Notification;

public class NotificationDetailActivity extends AppCompatActivity {
    private ActivityNotificationDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Notification notif = (Notification) getIntent().getSerializableExtra("notification");
        if (notif == null) {
            finish();
            return;
        }

        setupUI(notif);
    }

    private void setupUI(Notification notif) {
        binding.tvTitle.setText(notif.getTitle());
        binding.tvContent.setText(notif.getContent());
        binding.tvTime.setText(notif.getTime());

        // Set Icon based on type
        int iconRes = R.drawable.ic_notifications;
        if ("PROMOTION".equals(notif.getType())) iconRes = R.drawable.ic_gift;
        else if ("TICKET".equals(notif.getType())) iconRes = R.drawable.ic_ticket;
        else if ("PROFILE".equals(notif.getType())) iconRes = R.drawable.ic_profile;
        binding.imgType.setImageResource(iconRes);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnExplore.setOnClickListener(v -> {
            String typeStr = notif.getType();
            String title = notif.getTitle().toLowerCase();
            NotificationHelper.NotifType type = NotificationHelper.NotifType.SYSTEM;

            if (typeStr != null && !typeStr.isEmpty()) {
                try {
                    type = NotificationHelper.NotifType.valueOf(typeStr);
                } catch (Exception ignored) {}
            } else {
                // Tự động đoán loại dựa trên tiêu đề nếu không có Type (Legacy data)
                if (title.contains("chào mừng") || title.contains("hạng") || title.contains("thành viên")) {
                    type = NotificationHelper.NotifType.PROFILE;
                } else if (title.contains("khuyến mãi") || title.contains("ưu đãi")) {
                    type = NotificationHelper.NotifType.PROMOTION;
                } else if (title.contains("vé") || title.contains("đặt chỗ") || title.contains("thành công")) {
                    type = NotificationHelper.NotifType.TICKET;
                }
            }

            NotificationHelper.handleNotificationClick(this, type, notif.getTargetData());
            finish();
        });
    }
}
