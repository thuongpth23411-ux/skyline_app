package com.skyline.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityRankDetailsBinding;
import com.skyline.app.databinding.ItemBenefitBinding;
import com.skyline.app.network.AuthResponse;
import com.skyline.app.network.RankBenefit;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.User;
import com.skyline.app.utils.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankDetailsActivity extends AppCompatActivity {
    private ActivityRankDetailsBinding binding;
    private SessionManager sessionManager;
    private String currentRank = "Bạc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRankDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        setupClicks();
        loadData();
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.tabCurrent.setOnClickListener(v -> {
            updateTabUI(true);
            loadBenefits(currentRank);
        });

        binding.tabNext.setOnClickListener(v -> {
            updateTabUI(false);
            loadBenefits(getNextRank(currentRank));
        });
    }

    private void updateTabUI(boolean isCurrentSelected) {
        binding.tabCurrent.setBackgroundResource(isCurrentSelected ? R.drawable.bg_terms_rank : 0);
        binding.tabCurrent.setBackgroundTintList(isCurrentSelected ? android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white)) : null);
        binding.tabCurrent.setTypeface(null, isCurrentSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        binding.tabCurrent.setTextColor(getResources().getColor(isCurrentSelected ? R.color.skyline_text : R.color.skyline_text_secondary));

        binding.tabNext.setBackgroundResource(!isCurrentSelected ? R.drawable.bg_terms_rank : 0);
        binding.tabNext.setBackgroundTintList(!isCurrentSelected ? android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white)) : null);
        binding.tabNext.setTypeface(null, !isCurrentSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        binding.tabNext.setTextColor(getResources().getColor(!isCurrentSelected ? R.color.skyline_text : R.color.skyline_text_secondary));
    }

    private void loadData() {
        if (!sessionManager.isLoggedIn()) return;
        
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String rank = user.getRank() != null ? user.getRank() : "Đồng";
                    currentRank = rank;
                    displayUserInfo(user);
                    loadBenefits(rank);
                } else {
                    toast("Không thể tải thông tin hạng: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                toast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayUserInfo(User user) {
        if (user == null) return;
        
        String rank = user.getRank() != null ? user.getRank() : "Đồng";
        binding.tvRankName.setText("Hạng " + rank);
        binding.tvPoints.setText(String.valueOf(user.getSkyPoints()));
        binding.tvStartRank.setText("Hạng " + rank);
        String nextRank = getNextRank(rank);
        binding.tvEndRank.setText("Hạng " + nextRank);
        binding.tabCurrent.setText("Hạng hiện tại (" + rank + ")");
        binding.tabNext.setText("Hạng kế tiếp (" + nextRank + ")");

        // Set Background Gradient and Colors based on Rank
        updateRankCardStyle(rank);

        // Progress logic
        int points = user.getSkyPoints();
        int maxPoints = 1000;
        if ("Bạc".equalsIgnoreCase(rank)) maxPoints = 3000;
        else if ("Vàng".equalsIgnoreCase(rank)) maxPoints = 5000;
        
        int needed = maxPoints - points;
        if (needed > 0) {
            binding.tvPointsNeeded.setText("Còn " + needed + " điểm để lên Hạng " + nextRank);
            binding.progressRank.setProgress(points * 100 / maxPoints);
        } else {
            binding.tvPointsNeeded.setText("Bạn đã đạt hạng cao nhất");
            binding.progressRank.setProgress(100);
        }
    }

    private void updateRankCardStyle(String rank) {
        int bgRes;
        int progressColor;
        int mainTextColor;
        int secondaryTextColor;
        int iconBg;
        int iconTint;

        if ("Bạc".equalsIgnoreCase(rank)) {
            bgRes = R.drawable.bg_rank_silver_gradient;
            progressColor = 0xFF455A64;
            mainTextColor = 0xFF263238; // Deep Blue Grey
            secondaryTextColor = 0xFF546E7A;
            iconBg = 0x33455A64;
            iconTint = 0xFF455A64;
        } else if ("Vàng".equalsIgnoreCase(rank)) {
            bgRes = R.drawable.bg_rank_gold_gradient;
            progressColor = 0xFFBF953F;
            mainTextColor = 0xFF5D4037; // Dark Brown
            secondaryTextColor = 0xFF795548;
            iconBg = 0x33BF953F;
            iconTint = 0xFFBF953F;
        } else { // Đồng
            bgRes = R.drawable.bg_rank_bronze_gradient;
            progressColor = 0xFF8D6E63;
            mainTextColor = 0xFF3E2723; // Very Dark Brown
            secondaryTextColor = 0xFF5D4037;
            iconBg = 0x338D6E63;
            iconTint = 0xFF8D6E63;
        }

        binding.layoutRankCard.setBackgroundResource(bgRes);
        binding.progressRank.setIndicatorColor(progressColor);
        binding.progressRank.setTrackColor(0x20000000); 
        
        binding.tvRankName.setTextColor(mainTextColor);
        binding.tvPoints.setTextColor(mainTextColor);
        binding.tvStartRank.setTextColor(secondaryTextColor);
        binding.tvEndRank.setTextColor(secondaryTextColor);
        
        binding.imgRankIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(iconBg));
        binding.imgRankIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconTint));
        
        binding.tvPointsNeeded.setTextColor(mainTextColor);
        binding.tvPointsNeeded.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(mainTextColor));
    }

    private String getNextRank(String rank) {
        if ("Đồng".equalsIgnoreCase(rank)) return "Bạc";
        if ("Bạc".equalsIgnoreCase(rank)) return "Vàng";
        return "Kim cương";
    }

    private void loadBenefits(String rank) {
        if (rank == null) rank = "Đồng";
        binding.layoutBenefits.removeAllViews();
        RetrofitClient.getInstance().getRankBenefits(rank).enqueue(new Callback<List<RankBenefit>>() {
            @Override
            public void onResponse(Call<List<RankBenefit>> call, Response<List<RankBenefit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RankBenefit> benefits = response.body();
                    if (benefits.isEmpty()) {
                        toast("Hạng này chưa có thông tin quyền lợi");
                    } else {
                        for (RankBenefit benefit : benefits) {
                            addBenefitView(benefit);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<RankBenefit>> call, Throwable t) {
                toast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void addBenefitView(RankBenefit benefit) {
        ItemBenefitBinding itemBinding = ItemBenefitBinding.inflate(getLayoutInflater(), binding.layoutBenefits, false);
        itemBinding.tvBenefitTitle.setText(benefit.getTitle());
        itemBinding.tvBenefitDesc.setText(benefit.getDescription());
        
        // Set icon based on iconType
        int iconRes = R.drawable.ic_notifications;
        if ("points".equals(benefit.getIconType())) iconRes = R.drawable.ic_trending_up;
        else if ("checkin".equals(benefit.getIconType())) iconRes = R.drawable.ic_priority;
        else if ("voucher".equals(benefit.getIconType())) iconRes = R.drawable.ic_gift;
        
        itemBinding.imgBenefitIcon.setImageResource(iconRes);
        binding.layoutBenefits.addView(itemBinding.getRoot());
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
