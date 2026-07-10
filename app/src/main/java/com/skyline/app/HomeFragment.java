package com.skyline.app;

import android.util.Log;
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
import com.skyline.app.utils.NotificationHelper;
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
        updateNotificationBadge();
        checkNewPromotions();
    }

    private void updateNotificationBadge() {
        SessionManager sessionManager = new SessionManager(requireContext());
        int count = sessionManager.getUnreadNotifCount();
        if (count > 0) {
            binding.tvNotifBadge.setVisibility(View.VISIBLE);
            binding.tvNotifBadge.setText(String.valueOf(count));
        } else {
            binding.tvNotifBadge.setVisibility(View.GONE);
        }
    }

    private void checkNewPromotions() {
        Log.d("HomeFragment", "Simulating various notifications...");
        // Giả lập nhận các loại thông báo khác nhau sau một khoảng trễ ngắn
        if (binding != null) {
            binding.getRoot().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    NotificationHelper.showDropDownNotification(
                        getActivity(), 
                        "Ưu đãi vé bay", 
                        "Giảm ngay 20% khi đặt vé đi Đà Nẵng hôm nay!", 
                        NotificationHelper.NotifType.PROMOTION, 
                        "Thứ 6 Mở App"
                    );
                    updateNotificationBadge();
                }
            }, 3000);

            binding.getRoot().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    NotificationHelper.showDropDownNotification(
                        getActivity(), 
                        "Cập nhật hồ sơ", 
                        "Chúc mừng! Bạn đã được thăng hạng hội viên BẠC.", 
                        NotificationHelper.NotifType.PROFILE, 
                        null
                    );
                    updateNotificationBadge();
                }
            }, 8000);
        }
    }

    private void showNotificationPopup(Promotion promotion) {
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
        updateNotificationBadge();
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
        Log.d("HomeFragment", "Fetching promotions...");
        com.skyline.app.network.RetrofitClient.getInstance().getPromotions().enqueue(new retrofit2.Callback<List<Promotion>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Promotion>> call, retrofit2.Response<List<Promotion>> response) {
                if (binding == null || !isAdded()) return;

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Promotion> allPromotions = response.body();
                    Log.d("HomeFragment", "Promotions received: " + allPromotions.size());
                    
                    // Sắp xếp: Ưu tiên cái có tên "Thứ 6 Mở App - Giảm đến 10%" lên đầu
                    List<Promotion> filtered = new ArrayList<>();
                    Promotion priorityItem = null;
                    
                    for (Promotion p : allPromotions) {
                        if (p.getTitle().contains("Thứ 6 Mở App")) {
                            priorityItem = p;
                            break;
                        }
                    }
                    
                    if (priorityItem != null) {
                        filtered.add(priorityItem);
                    }
                    
                    for (Promotion p : allPromotions) {
                        if (p != priorityItem) {
                            filtered.add(p);
                        }
                        if (filtered.size() == 3) break;
                    }
                    
                    List<Promotion> displayList = filtered.isEmpty() ? allPromotions : filtered;
                    if (displayList.size() > 3) displayList = displayList.subList(0, 3);
                    
                    binding.promoPager.setAdapter(new PromotionAdapter(displayList, item -> toast("Đã chọn: " + item.getTitle())));
                    createDots(binding.promoDots, displayList.size());
                    selectDot(binding.promoDots, 0);

                    binding.promoPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            if (binding != null) selectDot(binding.promoDots, position);
                        }
                    });
                } else {
                    Log.e("HomeFragment", "Promotions response error or empty: " + response.code());
                    setupLocalPromotions(); // Vẫn giữ fallback nhưng log lỗi rõ ràng
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Promotion>> call, Throwable t) {
                Log.e("HomeFragment", "Promotions API failure: " + t.getMessage());
                if (binding != null && isAdded()) {
                    setupLocalPromotions();
                }
            }
        });
    }

    private void setupLocalPromotions() {
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion("Ưu đãi bay Bangkok", "Hết hạn: 31/12/2025", R.drawable.img_promo_bangkok));
        promotions.add(new Promotion("Khám phá Phú Quốc", "Hết hạn: 30/11/2025", R.drawable.img_promo_phuquoc));

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

    private void setupDestinations() {
        Log.d("HomeFragment", "Fetching destination blogs...");
        com.skyline.app.network.RetrofitClient.getInstance().getDestinationBlogs().enqueue(new retrofit2.Callback<List<Destination>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Destination>> call, retrofit2.Response<List<Destination>> response) {
                if (binding == null || !isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Destination> allBlogs = response.body();
                    Log.d("HomeFragment", "Blogs received: " + allBlogs.size());
                    
                    // Lấy 3 bài viết đầu tiên từ server
                    List<Destination> displayList = allBlogs.size() > 3 ? allBlogs.subList(0, 3) : allBlogs;

                    binding.destinationRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                    binding.destinationRecycler.setAdapter(new DestinationAdapter(displayList, item -> {
                        if (item.getBlogSlug() != null && !item.getBlogSlug().isEmpty()) {
                            Intent intent = new Intent(requireContext(), BlogDetailActivity.class);
                            intent.putExtra("slug", item.getBlogSlug());
                            startActivity(intent);
                        } else {
                            toast("Khám phá " + item.getCountry());
                        }
                    }));
                } else {
                    Log.e("HomeFragment", "Blogs response error: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Destination>> call, Throwable t) {
                Log.e("HomeFragment", "Blogs API failure: " + t.getMessage());
            }
        });
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
                sessionManager.clearUnreadNotifCount();
                updateNotificationBadge();
                startActivity(new Intent(requireContext(), NotificationActivity.class));
            } else {
                showLoginRequiredDialog();
            }
        });

        // Nhấn "Khám phá ngay" hoặc "Xem tất cả" mở trang Khuyến mãi
        binding.btnExploreNow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PromotionsActivity.class);
            intent.putExtra("OPEN_PROMO_NAME", "Thứ 6 Mở App");
            startActivity(intent);
        });

        binding.promotionHeader.tvViewAll.setOnClickListener(v -> startActivity(new Intent(requireContext(), PromotionsActivity.class)));

        binding.destinationHeader.tvViewAll.setOnClickListener(v -> startActivity(new Intent(requireContext(), BlogListActivity.class)));
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
