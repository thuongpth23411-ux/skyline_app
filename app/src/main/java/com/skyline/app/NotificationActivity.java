package com.skyline.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.ActivityNotificationBinding;
import com.skyline.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        com.skyline.app.utils.SessionManager sessionManager = new com.skyline.app.utils.SessionManager(this);
        sessionManager.clearUnreadNotifCount();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnClearAll.setOnClickListener(v -> showClearAllDialog());

        setupList();
    }

    private void showClearAllDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xóa tất cả thông báo")
            .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử thông báo không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                com.skyline.app.utils.SessionManager sm = new com.skyline.app.utils.SessionManager(this);
                sm.clearNotifications();
                setupList();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void setupList() {
        com.skyline.app.utils.SessionManager sessionManager = new com.skyline.app.utils.SessionManager(this);
        List<Notification> list = new ArrayList<>();
        
        String data = sessionManager.getLocalNotifications();
        if (!data.isEmpty()) {
            String[] entries = data.split(";");
            for (String entry : entries) {
                String[] parts = entry.split("\\|");
                if (parts.length >= 6) {
                    // id|title|content|time|type|targetData
                    list.add(new Notification(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                } else if (parts.length >= 5) {
                    // id|title|content|time|type
                    list.add(new Notification(parts[0], parts[1], parts[2], parts[3], parts[4], null));
                } else if (parts.length >= 4) {
                    // id|title|content|time
                    list.add(new Notification(parts[0], parts[1], parts[2], parts[3], null, null));
                } else if (parts.length >= 3) {
                    // Fallback for very old data: title|content|time
                    list.add(new Notification("", parts[0], parts[1], parts[2], null, null));
                }
            }
        }

        if (list.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
            binding.btnClearAll.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.btnClearAll.setVisibility(View.VISIBLE);
            binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotifications.setAdapter(new NotificationAdapter(list));
        }
    }
}
