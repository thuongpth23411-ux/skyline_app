package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import com.skyline.app.databinding.FragmentHomeBinding;
import com.skyline.app.network.Promotion;
import com.skyline.app.utils.SessionManager;
import com.skyline.model.Destination;
import com.skyline.model.Experience;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSectionTitles();
        setupPromotionPager();
        setupDestinations();
        setupExperiences();
        setupClicks();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        SessionManager sessionManager = new SessionManager(requireContext());
        if (sessionManager.isLoggedIn()) {
            binding.memberCard.getRoot().setVisibility(View.GONE);
        } else {
            binding.memberCard.getRoot().setVisibility(View.VISIBLE);
        }
    }

    private void setupSectionTitles() {
        binding.promotionHeader.tvSectionTitle.setText(getString(R.string.promotion_program));
        binding.destinationHeader.tvSectionTitle.setText(getString(R.string.discover_destinations));
    }

    private void setupPromotionPager() {
        // Chỉ nạp dữ liệu Banner du lịch cố định cho trang chủ cho đẹp
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion("Xin chào Bangkok! Ưu đãi ngay 20%", "10/07/2026", R.drawable.img_promo_bangkok));
        promotions.add(new Promotion("Xin chào Phú Quốc! Ưu đãi ngay 20%", "15/07/2026", R.drawable.img_promo_phuquoc));
        promotions.add(new Promotion("Thứ 6 mở app – giảm đến 10%", "01/08/2026", R.drawable.img_brand_banner));

        binding.promoPager.setAdapter(new PromotionAdapter(promotions, item -> toast("Đã chọn: " + item.getTitle())));
        createDots(binding.promoDots, promotions.size());
        selectDot(binding.promoDots, 0);

        binding.promoPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (binding != null) selectDot(binding.promoDots, position);
            }
        });
    }

    private void setupLocalPromotions() {
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion("Xin chào Bangkok! Ưu đãi ngay 20%", "11/06/2026 - 10/07/2026", R.drawable.img_promo_bangkok));
        promotions.add(new Promotion("Xin chào Phú Quốc! Ưu đãi ngay 20%", "15/06/2026 - 15/07/2026", R.drawable.img_promo_phuquoc));
        promotions.add(new Promotion("Thứ 6 mở app – giảm đến 10%", "17/06/2026 - 01/07/2026", R.drawable.img_brand_banner));

        binding.promoPager.setAdapter(new PromotionAdapter(promotions, item -> toast("Đã chọn: " + item.getTitle())));
        createDots(binding.promoDots, promotions.size());
        selectDot(binding.promoDots, 0);

        binding.promoPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectDot(binding.promoDots, position);
            }
        });
    }

    private void setupDestinations() {
        List<Destination> destinations = new ArrayList<>();
        destinations.add(new Destination("Việt Nam", "Phú Quốc – “đảo ngọc” xinh đẹp của Việt Nam", R.drawable.img_destination_phuquoc));
        destinations.add(new Destination("Việt Nam", "Đà Nẵng – thành phố đáng sống nhất Việt Nam", R.drawable.img_destination_danang));

        binding.destinationRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.destinationRecycler.setAdapter(new DestinationAdapter(destinations, item -> toast("Khám phá " + item.getTitle())));
    }

    private void setupExperiences() {
        List<Experience> experiences = new ArrayList<>();
        experiences.add(new Experience("Mức giá tốt, dịch vụ chu đáo.", "Hạng Phổ thông", "Thoải mái trong mọi hành trình", R.drawable.img_experience_economy));
        experiences.add(new Experience("Nâng tầm trải nghiệm", "Hạng Thương gia", "Không gian riêng tư, dịch vụ tinh tế.", R.drawable.img_experience_first));
        // Duplicate to ensure smooth infinite scroll and show at least 3 cards
        experiences.add(new Experience("Mức giá tốt, dịch vụ chu đáo.", "Hạng Phổ thông", "Thoải mái trong mọi hành trình", R.drawable.img_experience_economy));
        experiences.add(new Experience("Nâng tầm trải nghiệm", "Hạng Thương gia", "Không gian riêng tư, dịch vụ tinh tế.", R.drawable.img_experience_first));

        binding.experiencePager.setAdapter(new ExperienceAdapter(experiences, item -> toast("Đã chọn " + item.getTitle())));
        binding.experiencePager.setOffscreenPageLimit(3);
        
        // Start from a middle position for infinite effect
        int middle = Integer.MAX_VALUE / 2;
        int startPos = middle - (middle % experiences.size());
        binding.experiencePager.setCurrentItem(startPos, false);

        binding.experiencePager.setPageTransformer((page, position) -> {
            float scaleFactor = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.6f + (1 - Math.abs(position)) * 0.4f);
        });

        binding.btnExpPrev.setOnClickListener(v -> binding.experiencePager.setCurrentItem(binding.experiencePager.getCurrentItem() - 1));
        binding.btnExpNext.setOnClickListener(v -> binding.experiencePager.setCurrentItem(binding.experiencePager.getCurrentItem() + 1));
    }

    private void setupClicks() {
        binding.btnNotification.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(requireContext());
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(requireContext(), NotificationActivity.class));
            } else {
                showLoginRequiredDialog();
            }
        });

        // Nhấn "Khám phá ngay" hoặc "Xem tất cả" mở trang Khuyến mãi
        binding.btnExploreNow.setOnClickListener(v -> startActivity(new Intent(requireContext(), PromotionsActivity.class)));
        binding.promotionHeader.tvViewAll.setOnClickListener(v -> startActivity(new Intent(requireContext(), PromotionsActivity.class)));

        binding.destinationHeader.tvViewAll.setOnClickListener(v -> toast("Tất cả điểm đến"));
        binding.btnAboutUs.setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutActivity.class)));
        binding.memberCard.btnRegister.setOnClickListener(v -> startActivity(new Intent(requireContext(), RegisterEmailActivity.class)));
        binding.memberCard.tvLogin.setOnClickListener(v -> startActivity(new Intent(requireContext(), LoginActivity.class)));
    }

    private void showLoginRequiredDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Yêu cầu đăng nhập")
            .setMessage("Vui lòng đăng nhập hoặc đăng ký tài khoản để xem các thông báo cá nhân và ưu đãi từ Skyline.")
            .setPositiveButton("Đăng nhập", (dialog, which) -> startActivity(new Intent(requireContext(), LoginActivity.class)))
            .setNegativeButton("Để sau", null)
            .show();
    }

    private void createDots(LinearLayout container, int count) {
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            int size = dpToPx(9);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dpToPx(5), 0, dpToPx(5), 0);
            dot.setLayoutParams(params);
            dot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dot));
            container.addView(dot);
        }
    }

    private void selectDot(LinearLayout container, int selected) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View dot = container.getChildAt(i);
            dot.setBackground(ContextCompat.getDrawable(requireContext(), i == selected ? R.drawable.bg_dot_active : R.drawable.bg_dot));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
