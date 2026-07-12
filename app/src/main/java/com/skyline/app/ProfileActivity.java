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
import com.skyline.app.utils.QrGenerator;
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
        
        // 1. HIỂN THỊ TỨC THÌ TỪ BỘ NHỚ MÁY (Tránh trễ)
        binding.tvUsername.setText(sessionManager.getUserName());
        binding.tvCardNumber.setText(sessionManager.getMemberCode());
        int savedPoints = sessionManager.getUserPoints();
        binding.tvSkyPoints.setText(String.valueOf(savedPoints));
        updateRankUI(savedPoints);
        
        String savedAvatar = sessionManager.getUserAvatar();
        loadAvatar(savedAvatar);

        // 2. TẢI DỮ LIỆU MỚI NHẤT TỪ SERVER ĐỂ ĐỒNG BỘ
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    sessionManager.saveUser(user);
                    
                    binding.tvUsername.setText(user.getName());
                    binding.tvCardNumber.setText(user.getMemberCode());
                    binding.tvSkyPoints.setText(String.valueOf(user.getSkyPoints()));
                    updateRankUI(user.getSkyPoints());
                    loadAvatar(user.getAvatarUrl());

                    if (user.getMemberCode() != null) {
                        android.graphics.Bitmap qr = QrGenerator.generateQrCode(user.getMemberCode(), 200);
                        if (qr != null) binding.imgCardQr.setImageBitmap(qr);
                    }
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void loadAvatar(String url) {
        if (url == null || url.isEmpty()) return;
        String fullUrl = url.startsWith("/") ? "http://10.0.2.2:3000" + url : url;
        com.bumptech.glide.Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.img_team1)
                .error(R.drawable.img_team1)
                .into(binding.imgAvatar);
    }

    private void updateRankUI(int points) {
        String actualRank;
        String nextRank;
        if (points < 1000) {
            actualRank = "ĐỒNG";
            nextRank = "BẠC";
        } else if (points < 5000) {
            actualRank = "BẠC";
            nextRank = "VÀNG";
        } else {
            actualRank = "VÀNG";
            nextRank = "KIM CƯƠNG";
        }

        binding.tvUserRank.setText("HẠNG " + actualRank);
        binding.tvCurrentRankCard.setText(actualRank);
        binding.tvNextRankCard.setText(nextRank);
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
        // Logo trong thẻ hội viên click quay về Home
        binding.imgLogoCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

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
            android.app.Dialog dialog = new android.app.Dialog(this);
            View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_logout, null);
            dialog.setContentView(dialogView);
            
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            dialogView.findViewById(R.id.btn_cancel).setOnClickListener(d -> dialog.dismiss());
            
            dialogView.findViewById(R.id.btn_logout_confirm).setOnClickListener(d -> {
                dialog.dismiss();
                sessionManager.logout();
                Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });

            dialog.show();
        });

        binding.btnViewDetails.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, RankDetailsActivity.class));
        });

        binding.btnMyVouchers.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MyVouchersActivity.class));
        });

        binding.layoutCardQr.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MemberQrActivity.class);
            intent.putExtra("RANK", binding.tvUserRank.getText().toString().replace("HẠNG ", ""));
            startActivity(intent);
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
