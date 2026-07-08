package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.ActivityAboutBinding;
import com.skyline.app.utils.SessionManager;
import com.skyline.model.TeamMember;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupFeatures();
        setupTeam();
        setupReasons();
        setupClicks();
    }

    private void setupFeatures() {
        binding.featureEasy.tvFeatureTitle.setText("Đặt vé dễ dàng\nNhanh chóng & Tiện lợi");
        binding.featureEasy.tvFeatureDesc.setText("Tra cứu hàng trăm chuyến bay chỉ với vài thao tác tối ưu giao diện.");
        binding.featureEasy.imgFeature.setImageResource(R.drawable.img_booking_easy);

        binding.featurePrice.tvFeatureTitle.setText("Giá vé minh bạch\nKhông phí ẩn");
        binding.featurePrice.tvFeatureDesc.setText("Skyline cam kết hiển thị đúng giá, đúng thuế, đúng phí niêm yết.");
        binding.featurePrice.imgFeature.setImageResource(R.drawable.img_transparent_price);

        binding.featureSupport.tvFeatureTitle.setText("Hỗ trợ 24/7\nLuôn đồng hành");
        binding.featureSupport.tvFeatureDesc.setText("Đội ngũ hỗ trợ của Skyline sẵn sàng tư vấn mọi lúc, giải đáp mọi vấn đề.");
        binding.featureSupport.imgFeature.setImageResource(R.drawable.img_support_247);
    }

    private void setupTeam() {
        List<TeamMember> members = new ArrayList<>();
        members.add(new TeamMember("Trịnh Thị Thùy Trang", R.drawable.img_team1));
        members.add(new TeamMember("Phạm Thị Hoài Thương", R.drawable.img_team2));
        members.add(new TeamMember("Đào Thị Cẩm Vy", R.drawable.img_team3));
        members.add(new TeamMember("Trần Thị Thiên Thảo", R.drawable.img_team4));
        members.add(new TeamMember("Nguyễn Ngọc Tường Vy", R.drawable.img_team5));

        binding.teamPager.setAdapter(new TeamAdapter(members));
        binding.teamPager.setOffscreenPageLimit(3);

        // Start from a middle position for infinite effect
        if (!members.isEmpty()) {
            int middle = Integer.MAX_VALUE / 2;
            int startPos = middle - (middle % members.size());
            binding.teamPager.setCurrentItem(startPos, false);
        }
        
        binding.teamPager.setPageTransformer((page, position) -> {
            float scaleFactor = 0.82f + (1 - Math.abs(position)) * 0.18f;
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });
    }

    private void setupReasons() {
        binding.reasonPrice.tvReasonTitle.setText("Nội dung & giá vé\nđáng tin cậy");
        binding.reasonPrice.tvReasonDesc.setText("Báo giá minh bạch, cập nhật theo thời gian thực từ các hãng bay.");
        binding.reasonPrice.imgReasonIcon.setImageResource(R.drawable.ic_calendar_auth);

        binding.reasonTools.tvReasonTitle.setText("Công cụ quản lý hành\ntrình thông minh");
        binding.reasonTools.tvReasonDesc.setText("Theo dõi đặt chỗ, hành lý và thông báo tự động – all in one.");
        binding.reasonTools.imgReasonIcon.setImageResource(R.drawable.ic_home);

        binding.reasonSupport.tvReasonTitle.setText("Đội ngũ hỗ trợ\ntận tâm");
        binding.reasonSupport.tvReasonDesc.setText("Skyline cam kết đồng hành, giúp bạn an tâm tuyệt đối.");
        binding.reasonSupport.imgReasonIcon.setImageResource(R.drawable.ic_profile);
    }

    private void setupClicks() {
        binding.btnNotification.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, NotificationActivity.class));
        });
        
        binding.btnTeamPrev.setOnClickListener(v -> binding.teamPager.setCurrentItem(binding.teamPager.getCurrentItem() - 1));
        binding.btnTeamNext.setOnClickListener(v -> binding.teamPager.setCurrentItem(binding.teamPager.getCurrentItem() + 1));

        binding.bottomNav.navHome.setOnClickListener(v -> finish());
        binding.bottomNav.navBook.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình Đặt vé", Toast.LENGTH_SHORT).show());
        binding.bottomNav.navFlights.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình Chuyến bay", Toast.LENGTH_SHORT).show());
        binding.bottomNav.navProfile.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }
}
