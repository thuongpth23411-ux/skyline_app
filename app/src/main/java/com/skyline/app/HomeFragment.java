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

        binding.promoPager.setAdapter(new PromotionAdapter(promotions, item -> {
            // Khi nhấn vào item trong Pager (có chữ "Tìm hiểu thêm"), nhảy qua trang Khuyến mãi
            startActivity(new Intent(requireContext(), PromotionsActivity.class));
        }));
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
        experiences.add(new Experience(getString(R.string.exp_economy_desc), getString(R.string.exp_economy_title), getString(R.string.exp_economy_sub), R.drawable.img_experience_economy));
        experiences.add(new Experience(getString(R.string.exp_first_desc), getString(R.string.exp_first_title), getString(R.string.exp_first_sub), R.drawable.img_experience_first));
        experiences.add(new Experience(getString(R.string.exp_economy_desc), getString(R.string.exp_economy_title), getString(R.string.exp_economy_sub), R.drawable.img_experience_economy));
        experiences.add(new Experience(getString(R.string.exp_first_desc), getString(R.string.exp_first_title), getString(R.string.exp_first_sub), R.drawable.img_experience_first));

        binding.experiencePager.setAdapter(new ExperienceAdapter(experiences, item -> {
            if (item.getTitle().equals(getString(R.string.exp_economy_title))) {
                openEconomyExperienceBlog();
            } else if (item.getTitle().equals(getString(R.string.exp_business_title)) || item.getTitle().contains("Thương gia")) {
                openBusinessExperienceBlog();
            } else {
                toast("Đã chọn " + item.getTitle());
            }
        }));
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

    private void openEconomyExperienceBlog() {
        com.skyline.app.network.Blog economyBlog = new com.skyline.app.network.Blog();
        economyBlog.title = "Hạng phổ thông - Hành trình của sự chăm chút";
        economyBlog.category = "TRẢI NGHIỆM";
        economyBlog.coverImageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.phothong_banner;
        economyBlog.introContent = "Một chuyến đi trọn vẹn không nhất thiết phải bắt đầu từ hạng vé cao cấp, mà từ việc lựa chọn đúng chuyến bay, đúng quyền lợi và đúng nhu cầu. Với hạng Phổ thông, hành khách có thể cân bằng giữa chi phí, thời gian và sự thuận tiện. Skyline giúp tổng hợp chuyến bay từ nhiều hãng hàng không để việc tìm kiếm, so sánh và đặt vé trở nên dễ dàng, rõ ràng hơn.";
        economyBlog.readTime = "6 phút đọc";
        economyBlog.publishedDate = "2024-05-20T08:00:00Z";

        List<com.skyline.app.network.Blog.Section> sections = new ArrayList<>();

        // Section 1
        com.skyline.app.network.Blog.Section s1 = new com.skyline.app.network.Blog.Section();
        s1.sectionNumber = 1;
        s1.title = "Lựa chọn vừa vặn cho những hành trình thường ngày";
        s1.type = "text";
        com.skyline.app.network.Blog.SectionItem item1 = new com.skyline.app.network.Blog.SectionItem();
        item1.description = "Hạng Phổ thông là lựa chọn phổ biến của hành khách trên các chuyến bay nội địa và quốc tế. Mức giá hợp lý, nhiều khung giờ và đa dạng hãng bay giúp hạng vé này phù hợp with nhiều nhu cầu như du lịch, công tác, về quê hoặc thăm gia đình. Tùy từng hãng và từng loại giá vé, hành khách có thể nhận được các quyền lợi khác nhau về hành lý, lựa chọn chỗ ngồi, suất ăn và khả năng thay đổi lịch trình.";
        item1.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_pt_1;
        s1.items = new ArrayList<>();
        s1.items.add(item1);
        sections.add(s1);

        // Section 2
        com.skyline.app.network.Blog.Section s2 = new com.skyline.app.network.Blog.Section();
        s2.sectionNumber = 2;
        s2.title = "Một tấm vé phù hợp không chỉ được quyết định bởi giá thấp";
        s2.type = "text";
        com.skyline.app.network.Blog.SectionItem item2 = new com.skyline.app.network.Blog.SectionItem();
        item2.description = "Giá vé hiển thị ban đầu chưa phải lúc nào cũng là tổng chi phí của chuyến đi. Khi lựa chọn vé hạng Phổ thông, hành khách nên cân nhắc đồng thời giá vé, giờ bay, thời gian di chuyển, quyền lợi hành lý và điều kiện thay đổi vé. Một lựa chọn phù hợp là lựa chọn đáp ứng tốt nhu cầu thực tế, thay vì chỉ có mức giá thấp nhất.";
        item2.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_pt_2;
        s2.items = new ArrayList<>();
        s2.items.add(item2);
        sections.add(s2);

        // Section 3
        com.skyline.app.network.Blog.Section s3 = new com.skyline.app.network.Blog.Section();
        s3.sectionNumber = 3;
        s3.title = "Mỗi hãng hàng không mang đến một chính sách khác nhau";
        s3.type = "text";
        com.skyline.app.network.Blog.SectionItem item3 = new com.skyline.app.network.Blog.SectionItem();
        item3.description = "Dù cùng thuộc hạng Phổ thông, các hãng hàng không có thể áp dụng những chính sách và quyền lợi không giống nhau. Điều kiện đổi ngày, hoàn vé hoặc lựa chọn chỗ ngồi cũng có thể thay đổi theo từng hãng. Vì vậy, hành khách nên kiểm tra kỹ thông tin trước khi thanh toán để hiểu rõ những quyền lợi đã bao gồm.";
        item3.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_pt_3;
        s3.items = new ArrayList<>();
        s3.items.add(item3);
        sections.add(s3);

        // Section 4
        com.skyline.app.network.Blog.Section s4 = new com.skyline.app.network.Blog.Section();
        s4.sectionNumber = 4;
        s4.title = "So sánh đầy đủ để tìm ra chuyến bay phù hợp nhất";
        s4.type = "text";
        com.skyline.app.network.Blog.SectionItem item4 = new com.skyline.app.network.Blog.SectionItem();
        item4.description = "Thông qua Skyline, người dùng có thể xem và so sánh chuyến bay từ nhiều hãng hàng không trên cùng một nền tảng. Các thông tin về hãng bay, giờ khởi hành, thời gian đến, giá vé và điều kiện đi kèm được trình bày trực quan, giúp hành khách dễ dàng đưa ra quyết định phù hợp nhất với mình.";
        item4.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_pt_4;
        s4.items = new ArrayList<>();
        s4.items.add(item4);
        sections.add(s4);

        // Section 5
        com.skyline.app.network.Blog.Section s5 = new com.skyline.app.network.Blog.Section();
        s5.sectionNumber = 5;
        s5.title = "Một quy trình đặt vé nhẹ nhàng cho hành trình thêm an tâm";
        s5.type = "text";
        com.skyline.app.network.Blog.SectionItem item5 = new com.skyline.app.network.Blog.SectionItem();
        item5.description = "Skyline giúp đơn giản hóa quá trình đặt vé từ bước tìm kiếm chuyến bay đến khi hoàn tất thông tin hành khách. Sự rõ ràng trong từng bước giúp hạn chế nhầm lẫn và mang lại cảm giác an tâm hơn khi chuẩn bị cho chuyến đi. Hạng Phổ thông không chỉ là lựa chọn tiết kiệm, mà còn có thể mang đến một hành trình thuận tiện.";
        item5.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_pt_5;
        s5.items = new ArrayList<>();
        s5.items.add(item5);
        sections.add(s5);

        economyBlog.sections = sections;
        
        com.skyline.app.network.Blog.CTA cta = new com.skyline.app.network.Blog.CTA();
        cta.text = "ĐẶT VÉ NGAY!";
        cta.action = "BOOK_FLIGHT";
        economyBlog.cta = cta;

        Intent intent = new Intent(requireContext(), BlogDetailActivity.class);
        intent.putExtra("blog", economyBlog);
        startActivity(intent);
    }

    private void openBusinessExperienceBlog() {
        com.skyline.app.network.Blog businessBlog = new com.skyline.app.network.Blog();
        businessBlog.title = "Hạng Thương gia – Hành trình của những đặc quyền";
        businessBlog.category = "TRẢI NGHIỆM";
        businessBlog.coverImageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.hangthuonggia_banner;
        businessBlog.introContent = "Hạng Thương gia không chỉ mang đến một chỗ ngồi rộng rãi hơn mà còn mở ra trải nghiệm di chuyển ưu tiên, riêng tư và tiện nghi trong suốt hành trình. Với Skyline, bạn có thể tìm kiếm, so sánh và lựa chọn vé hạng Thương gia của nhiều hãng hàng không trên cùng một nền tảng.";
        businessBlog.readTime = "8 phút đọc";
        businessBlog.publishedDate = "2024-05-20T08:00:00Z";

        List<com.skyline.app.network.Blog.Section> sections = new ArrayList<>();

        // Section 1
        com.skyline.app.network.Blog.Section s1 = new com.skyline.app.network.Blog.Section();
        s1.sectionNumber = 1;
        s1.title = "Trải nghiệm ưu tiên ngay từ sân bay";
        s1.type = "text";
        com.skyline.app.network.Blog.SectionItem item1 = new com.skyline.app.network.Blog.SectionItem();
        item1.description = "Hành khách hạng Thương gia thường được sử dụng quầy làm thủ tục riêng, ưu tiên lên máy bay và nhận hành lý sớm hơn sau khi hạ cánh. Một số hãng còn cung cấp lối ưu tiên tại khu vực kiểm tra an ninh, giúp tiết kiệm thời gian chờ đợi.";
        item1.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_tg_1;
        s1.items = new ArrayList<>();
        s1.items.add(item1);
        sections.add(s1);

        // Section 2
        com.skyline.app.network.Blog.Section s2 = new com.skyline.app.network.Blog.Section();
        s2.sectionNumber = 2;
        s2.title = "Không gian ghế ngồi riêng tư và thoải mái";
        s2.type = "text";
        com.skyline.app.network.Blog.SectionItem item2 = new com.skyline.app.network.Blog.SectionItem();
        item2.description = "Ghế hạng Thương gia thường có kích thước rộng, khoảng cách để chân thoải mái và khả năng điều chỉnh linh hoạt. Trên một số chuyến bay đường dài, ghế có thể ngả thành giường nằm, đi kèm không gian riêng tư và khu vực làm việc tiện lợi.";
        item2.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_tg_2;
        s2.items = new ArrayList<>();
        s2.items.add(item2);
        sections.add(s2);

        // Section 3
        com.skyline.app.network.Blog.Section s3 = new com.skyline.app.network.Blog.Section();
        s3.sectionNumber = 3;
        s3.title = "Dịch vụ và ẩm thực được nâng tầm";
        s3.type = "text";
        com.skyline.app.network.Blog.SectionItem item3 = new com.skyline.app.network.Blog.SectionItem();
        item3.description = "Trải nghiệm hạng Thương gia thường đi kèm phong cách phục vụ chu đáo, thực đơn đa dạng và cách trình bày chỉn chu hơn. Hành khách có thể được lựa chọn suất ăn, đồ uống hoặc đăng ký trước các yêu cầu ăn uống đặc biệt.";
        item3.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_tg_3;
        s3.items = new ArrayList<>();
        s3.items.add(item3);
        sections.add(s3);

        // Section 4
        com.skyline.app.network.Blog.Section s4 = new com.skyline.app.network.Blog.Section();
        s4.sectionNumber = 4;
        s4.title = "Hành lý linh hoạt và phòng chờ tiện nghi";
        s4.type = "text";
        com.skyline.app.network.Blog.SectionItem item4 = new com.skyline.app.network.Blog.SectionItem();
        item4.description = "Vé hạng Thương gia thường có hạn mức hành lý cao hơn. Bên cạnh đó, một số loại vé còn đi kèm quyền sử dụng phòng chờ sân bay với không gian nghỉ ngơi, Wi-Fi, đồ ăn nhẹ và khu vực làm việc yên tĩnh.";
        item4.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_tg_4;
        s4.items = new ArrayList<>();
        s4.items.add(item4);
        sections.add(s4);

        // Section 5
        com.skyline.app.network.Blog.Section s5 = new com.skyline.app.network.Blog.Section();
        s5.sectionNumber = 5;
        s5.title = "Dễ dàng so sánh vé hạng Thương gia trên Skyline";
        s5.type = "text";
        com.skyline.app.network.Blog.SectionItem item5 = new com.skyline.app.network.Blog.SectionItem();
        item5.description = "Trên Skyline, bạn có thể tìm kiếm và so sánh nhiều chuyến bay dựa trên giá vé, thời gian khởi hành, hạn mức hành lý và các điều kiện đi kèm. Nhờ đó, bạn có thể tận hưởng trải nghiệm hạng Thương gia một cách trọn vẹn nhất.";
        item5.imageUrl = "android.resource://" + requireContext().getPackageName() + "/" + R.drawable.blog_tg_5;
        s5.items = new ArrayList<>();
        s5.items.add(item5);
        sections.add(s5);

        businessBlog.sections = sections;
        
        com.skyline.app.network.Blog.CTA cta = new com.skyline.app.network.Blog.CTA();
        cta.text = "ĐẶT VÉ NGAY!";
        cta.action = "BOOK_FLIGHT";
        businessBlog.cta = cta;

        Intent intent = new Intent(requireContext(), BlogDetailActivity.class);
        intent.putExtra("blog", businessBlog);
        startActivity(intent);
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
        View.OnClickListener promoListener = v -> startActivity(new Intent(requireContext(), PromotionsActivity.class));

        binding.btnExploreNow.setOnClickListener(promoListener);
        binding.promotionHeader.tvViewAll.setOnClickListener(promoListener);

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
