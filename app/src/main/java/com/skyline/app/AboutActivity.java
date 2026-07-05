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
        binding.featureEasy.tvFeatureDesc.setText("Tại Skyline, người dùng có thể tra cứu hàng trăm chuyến bay chỉ với vài thao tác. Chúng tôi tối ưu giao diện, giảm số bước đặt vé và đảm bảo mọi thông tin đều rõ ràng, trực quan.");
        binding.featureEasy.imgFeature.setImageResource(R.drawable.img_booking_easy);

        binding.featurePrice.tvFeatureTitle.setText("Giá vé minh bạch\nKhông phí ẩn");
        binding.featurePrice.tvFeatureDesc.setText("Skyline cam kết hiển thị đúng giá, đúng thuế, đúng phí. Mọi chi phí đều được liệt kê rõ ràng giúp khách hàng an tâm khi thanh toán và lựa chọn chuyến bay phù hợp nhất.");
        binding.featurePrice.imgFeature.setImageResource(R.drawable.img_transparent_price);

        binding.featureSupport.tvFeatureTitle.setText("Hỗ trợ 24/7\nLuôn đồng hành cùng bạn");
        binding.featureSupport.tvFeatureDesc.setText("Đội ngũ hỗ trợ của Skyline sẵn sàng tư vấn mọi lúc, giải đáp vé, hành lý, thay đổi chuyến bay và các dịch vụ liên quan.");
        binding.featureSupport.imgFeature.setImageResource(R.drawable.img_support_247);
    }

    private void setupTeam() {
        List<TeamMember> members = new ArrayList<>();
        members.add(new TeamMember("Trịnh Thị Thùy Trang", R.drawable.img_team1));
        members.add(new TeamMember("Phạm Thị Hoài Thương", R.drawable.img_team2));
        members.add(new TeamMember("Đào Thị Cẩm Vy", R.drawable.img_team3));
        members.add(new TeamMember("Trần Thị Thiên Thảo", R.drawable.img_team4));
        members.add(new TeamMember("Nguyễn Ngọc Tường Vy", R.drawable.img_team5));

        binding.teamRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.teamRecycler.setAdapter(new TeamAdapter(members));
    }

    private void setupReasons() {
        binding.reasonPrice.tvIcon.setText("◷");
        binding.reasonPrice.tvReasonTitle.setText("Nội dung & giá vé\nđáng tin cậy");
        binding.reasonPrice.tvReasonDesc.setText("Báo giá minh bạch, cập nhật theo thời gian thực từ các hãng bay trong nước và quốc tế.");

        binding.reasonTools.tvIcon.setText("⌂");
        binding.reasonTools.tvReasonTitle.setText("Công cụ quản lý hành\ntrình thông minh");
        binding.reasonTools.tvReasonDesc.setText("Theo dõi đặt chỗ, hành lý, thay đổi chuyến bay và thông báo tự động – all in one.");

        binding.reasonSupport.tvIcon.setText("♧");
        binding.reasonSupport.tvReasonTitle.setText("Đội ngũ hỗ trợ\ntận tâm");
        binding.reasonSupport.tvReasonDesc.setText("Skyline cam kết đồng hành trước, trong và sau chuyến bay, giúp bạn an tâm tuyệt đối.");
    }

    private void setupClicks() {
        binding.btnNotification.setOnClickListener(v -> Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show());
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
