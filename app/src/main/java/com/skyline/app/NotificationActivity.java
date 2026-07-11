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

        binding.btnBack.setOnClickListener(v -> finish());

        setupList();
    }

    private void setupList() {
        com.skyline.app.utils.SessionManager sessionManager = new com.skyline.app.utils.SessionManager(this);
        List<Notification> list = new ArrayList<>();
        
        String data = sessionManager.getLocalNotifications();
        if (!data.isEmpty()) {
            String[] entries = data.split(";");
            for (String entry : entries) {
                String[] parts = entry.split("\\|");
                if (parts.length >= 3) {
                    list.add(new Notification(String.valueOf(list.size()), parts[0], parts[1], parts[2], "system", ""));
                }
            }
        }

        if (list.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotifications.setAdapter(new NotificationAdapter(list));
        }
    }
}
