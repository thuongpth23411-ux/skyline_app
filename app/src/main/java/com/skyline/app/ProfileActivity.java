package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.skyline.app.databinding.ActivityProfileBinding;
import com.skyline.app.network.AuthResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.User;
import com.skyline.app.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupBottomNavigation();
        setupMenuClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserInfo();
    }

    private void displayUserInfo() {
        if (!sessionManager.isLoggedIn()) {
            binding.btnLogoutItem.setVisibility(View.GONE);
            binding.tvUsername.setText("Chưa đăng nhập");
            binding.tvUserRank.setText("");
            binding.tvSkyPoints.setText("0");
            binding.tvCardNumber.setText("---- ---- ---- ----");
            return;
        }

        binding.btnLogoutItem.setVisibility(View.VISIBLE);
        
        // Hiển thị dữ liệu tạm thời từ SharedPreferences trong lúc tải
        binding.tvUsername.setText(sessionManager.getUserName());
        binding.tvCardNumber.setText(sessionManager.getMemberCode());

        String token = "Bearer " + sessionManager.fetchAuthToken();

        // Load dữ liệu mới nhất từ MongoDB qua API
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    
                    // Cập nhật lại SessionManager với dữ liệu mới nhất
                    sessionManager.saveUser(user);
                    
                    // Cập nhật UI từ MongoDB
                    binding.tvUsername.setText(user.getName());
                    binding.tvUserRank.setText("HẠNG " + (user.getRank() != null ? user.getRank().toUpperCase() : "ĐỒNG"));
                    binding.tvSkyPoints.setText(String.valueOf(user.getSkyPoints()));
                    binding.tvCardNumber.setText(user.getMemberCode());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Nếu lỗi, vẫn giữ dữ liệu cũ để người dùng không thấy trống
            }
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.navProfile.setSelected(true);

        View.OnClickListener goHomeListener = v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        };

        binding.bottomNavigation.navHome.setOnClickListener(goHomeListener);
        binding.bottomNavigation.tvNavHomeText.setOnClickListener(goHomeListener);

        binding.bottomNavigation.navBook.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "BOOK");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        binding.bottomNavigation.navFlights.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void setupMenuClicks() {
        binding.btnMenuTop.setOnClickListener(v -> {
            SettingsSideDialog dialog = new SettingsSideDialog();
            dialog.show(getSupportFragmentManager(), "SettingsSideDialog");
        });

        binding.btnSupport.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SupportActivity.class));
        });

        binding.itemMemberInfo.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MemberInfoActivity.class));
        });

        binding.itemTerms.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, TermsActivity.class));
        });

        binding.itemPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PrivacyActivity.class));
        });

        binding.btnPassengerDirectory.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PassengerDirectoryActivity.class));
        });

        binding.btnBookFlight.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "BOOK");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.btnLogoutItem.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        binding.btnViewDetails.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, RankDetailsActivity.class));
        });

        // Nhấn vào mục Voucher trong Profile để xem Voucher của tôi
        binding.btnMyVouchers.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MyVouchersActivity.class));
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
