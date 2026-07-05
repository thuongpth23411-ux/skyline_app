package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.skyline.app.databinding.ActivityProfileBinding;
import com.skyline.app.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        setupMenuClicks();
        displayUserInfo();
    }

    private void displayUserInfo() {
        android.content.SharedPreferences prefs = getSharedPreferences("skyline_prefs", MODE_PRIVATE);
        String name = prefs.getString("user_name", "Khách");
        binding.tvUsername.setText(name);
        binding.btnLogoutItem.setVisibility(View.VISIBLE);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.navProfile.setSelected(true);

        binding.bottomNavigation.navHome.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        });
        binding.bottomNavigation.navBook.setOnClickListener(v -> toast("Mở màn hình Đặt vé"));
        binding.bottomNavigation.navFlights.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupMenuClicks() {
        binding.itemMemberInfo.setOnClickListener(v -> toast("Thông tin hội viên"));
        binding.itemTerms.setOnClickListener(v -> toast("Điều khoản & Điều kiện"));
        binding.itemPrivacy.setOnClickListener(v -> toast("Chính sách bảo mật"));
        binding.btnLogoutItem.setOnClickListener(v -> {
            sessionManager.logout();
            toast("Đăng xuất thành công");
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        binding.btnViewDetails.setOnClickListener(v -> toast("Xem chi tiết điểm thưởng"));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
