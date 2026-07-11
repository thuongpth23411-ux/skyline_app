package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityBlogListBinding;
import com.skyline.app.network.Blog;
import com.skyline.app.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogListActivity extends AppCompatActivity {

    private ActivityBlogListBinding binding;
    private BlogAdapter latestAdapter;
    private FeaturedBlogAdapter featuredAdapter;
    
    private List<Blog> allBlogs = new ArrayList<>();
    private String selectedCategory = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlogListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActions();
        setupRecyclerViews();
        updateCategoryUI();
        loadBlogs();
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.chipDestination.setOnClickListener(v -> selectCategory("DIEM_DEN"));
        binding.chipTips.setOnClickListener(v -> selectCategory("KINH_NGHIEM"));
        binding.chipExperience.setOnClickListener(v -> selectCategory("EXPERIENCE"));

        binding.btnSubscribe.setOnClickListener(v -> {
            String email = binding.etSubscribeEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không đúng định dạng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đăng ký nhận bảng tin thành công!", Toast.LENGTH_SHORT).show();
                binding.etSubscribeEmail.setText("");
                binding.etSubscribeEmail.clearFocus();
            }
        });
    }

    private void setupRecyclerViews() {
        featuredAdapter = new FeaturedBlogAdapter(new ArrayList<>(), this::openBlogDetail);
        binding.rvFeaturedDestinations.setAdapter(featuredAdapter);

        latestAdapter = new BlogAdapter(new ArrayList<>(), this::openBlogDetail);
        binding.rvLatestArticles.setAdapter(latestAdapter);
    }

    private void loadBlogs() {
        RetrofitClient.getInstance().getBlogs().enqueue(new Callback<List<Blog>>() {
            @Override
            public void onResponse(Call<List<Blog>> call, Response<List<Blog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBlogs = response.body();
                    filterBlogs();
                }
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(BlogListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectCategory(String category) {
        if (selectedCategory.equals(category)) {
            selectedCategory = "ALL";
        } else {
            selectedCategory = category;
        }
        updateCategoryUI();
        filterBlogs();
    }

    private void updateCategoryUI() {
        int activeColor = android.graphics.Color.parseColor("#E0F2FE");
        int inactiveColor = android.graphics.Color.parseColor("#F3F4F6");
        int activeText = android.graphics.Color.parseColor("#0B4DA2");
        int inactiveText = android.graphics.Color.parseColor("#000000");

        binding.chipDestination.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(selectedCategory.equals("DIEM_DEN") ? activeColor : inactiveColor));
        binding.tvTextDest.setTextColor(selectedCategory.equals("DIEM_DEN") ? activeText : inactiveText);
        binding.ivIconDest.setImageTintList(android.content.res.ColorStateList.valueOf(selectedCategory.equals("DIEM_DEN") ? activeText : activeText));

        binding.chipTips.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(selectedCategory.equals("KINH_NGHIEM") ? activeColor : inactiveColor));
        binding.tvTextTips.setTextColor(selectedCategory.equals("KINH_NGHIEM") ? activeText : inactiveText);

        binding.chipExperience.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(selectedCategory.equals("EXPERIENCE") ? activeColor : inactiveColor));
        binding.tvTextExp.setTextColor(selectedCategory.equals("EXPERIENCE") ? activeText : inactiveText);
    }

    private void filterBlogs() {
        binding.tvSectionLatestTitle.setText("Bài viết mới nhất");
        if (selectedCategory.equals("EXPERIENCE")) {
            binding.layoutFeatured.setVisibility(android.view.View.GONE);
            latestAdapter.updateData(getExperienceBlogs());
        } else {
            binding.layoutFeatured.setVisibility(android.view.View.VISIBLE);
            
            List<Blog> featured = new ArrayList<>();
            List<Blog> latest = new ArrayList<>();
            
            for (Blog b : allBlogs) {
                boolean match = false;
                if (selectedCategory.equals("ALL")) match = true;
                else if (selectedCategory.equals("DIEM_DEN") && "ĐIỂM ĐẾN".equalsIgnoreCase(b.category)) match = true;
                else if (selectedCategory.equals("KINH_NGHIEM") && "KINH NGHIỆM".equalsIgnoreCase(b.category)) match = true;

                if (match) {
                    if (b.isFeatured) featured.add(b);
                    latest.add(b);
                }
            }
            
            featuredAdapter.updateData(featured);
            latestAdapter.updateData(latest);
            
            binding.layoutFeatured.setVisibility(featured.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        }
    }

    private List<Blog> getExperienceBlogs() {
        List<Blog> list = new ArrayList<>();
        list.add(createEconomyBlog());
        list.add(createBusinessBlog());
        return list;
    }

    private Blog createEconomyBlog() {
        Blog b = new Blog();
        b.title = "Hạng phổ thông - Hành trình của sự chăm chút";
        b.category = "TRẢI NGHIỆM";
        b.coverImageUrl = "android.resource://" + getPackageName() + "/" + R.drawable.phothong_banner;
        b.thumbnailUrl = b.coverImageUrl;
        b.introContent = "Một chuyến đi trọn vẹn không nhất thiết phải bắt đầu từ hạng vé cao cấp, mà từ việc lựa chọn đúng chuyến bay, đúng quyền lợi và đúng nhu cầu. Với hạng Phổ thông, hành khách có thể cân bằng giữa chi phí, thời gian và sự thuận tiện. Skyline giúp tổng hợp chuyến bay từ nhiều hãng hàng không để việc tìm kiếm, so sánh và đặt vé trở nên dễ dàng, rõ ràng hơn.";
        b.readTime = "6 phút đọc";
        b.publishedDate = "2024-05-20T08:00:00Z";

        List<Blog.Section> sections = new ArrayList<>();
        sections.add(createSection(1, "Lựa chọn vừa vặn cho những hành trình thường ngày", "Hạng Phổ thông là lựa chọn phổ biến của hành khách trên các chuyến bay nội địa và quốc tế. Mức giá hợp lý, nhiều khung giờ và đa dạng hãng bay giúp hạng vé này phù hợp with nhiều nhu cầu như du lịch, công tác, về quê hoặc thăm gia đình. Tùy từng hãng và từng loại giá vé, hành khách có thể nhận được các quyền lợi khác nhau về hành lý, lựa chọn chỗ ngồi, suất ăn và khả năng thay đổi lịch trình.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_pt_1));
        sections.add(createSection(2, "Một tấm vé phù hợp không chỉ được quyết định bởi giá thấp", "Giá vé hiển thị ban đầu chưa phải lúc nào cũng là tổng chi phí của chuyến đi. Khi lựa chọn vé hạng Phổ thông, hành khách nên cân nhắc đồng thời giá vé, giờ bay, thời gian di chuyển, quyền lợi hành lý và điều kiện thay đổi vé. Một lựa chọn phù hợp là lựa chọn đáp ứng tốt nhu cầu thực tế, thay vì chỉ có mức giá thấp nhất.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_pt_2));
        sections.add(createSection(3, "Mỗi hãng hàng không mang đến một chính sách khác nhau", "Dù cùng thuộc hạng Phổ thông, các hãng hàng không có thể áp dụng những chính sách và quyền lợi không giống nhau. Điều kiện đổi ngày, hoàn vé hoặc lựa chọn chỗ ngồi cũng có thể thay đổi theo từng hãng. Vì vậy, hành khách nên kiểm tra kỹ thông tin trước khi thanh toán để hiểu rõ những quyền lợi đã bao gồm.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_pt_3));
        sections.add(createSection(4, "So sánh đầy đủ để tìm ra chuyến bay phù hợp nhất", "Thông qua Skyline, người dùng có thể xem và so sánh chuyến bay từ nhiều hãng hàng không trên cùng một nền tảng. Các thông tin về hãng bay, giờ khởi hành, thời gian đến, giá vé và điều kiện đi kèm được trình bày trực quan, giúp hành khách dễ dàng đưa ra quyết định phù hợp nhất với mình.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_pt_4));
        sections.add(createSection(5, "Một quy trình đặt vé nhẹ nhàng cho hành trình thêm an tâm", "Skyline giúp đơn giản hóa quá trình đặt vé từ bước tìm kiếm chuyến bay đến khi hoàn tất thông tin hành khách. Sự rõ ràng trong từng bước giúp hạn chế nhầm lẫn và mang lại cảm giác an tâm hơn khi chuẩn bị cho chuyến đi. Hạng Phổ thông không chỉ là lựa chọn tiết kiệm, mà còn có thể mang đến một hành trình thuận tiện.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_pt_5));
        b.sections = sections;
        
        Blog.CTA cta = new Blog.CTA();
        cta.text = "ĐẶT VÉ NGAY!";
        cta.action = "BOOK_FLIGHT";
        b.cta = cta;
        return b;
    }

    private Blog createBusinessBlog() {
        Blog b = new Blog();
        b.title = "Hạng Thương gia – Hành trình của những đặc quyền";
        b.category = "TRẢI NGHIỆM";
        b.coverImageUrl = "android.resource://" + getPackageName() + "/" + R.drawable.hangthuonggia_banner;
        b.thumbnailUrl = b.coverImageUrl;
        b.introContent = "Hạng Thương gia không chỉ mang đến một chỗ ngồi rộng rãi hơn mà còn mở ra trải nghiệm di chuyển ưu tiên, riêng tư và tiện nghi trong suốt hành trình. Với Skyline, bạn có thể tìm kiếm, so sánh và lựa chọn vé hạng Thương gia của nhiều hãng hàng không trên cùng một nền tảng.";
        b.readTime = "8 phút đọc";
        b.publishedDate = "2024-05-20T08:00:00Z";

        List<Blog.Section> sections = new ArrayList<>();
        sections.add(createSection(1, "Trải nghiệm ưu tiên ngay từ sân bay", "Hành khách hạng Thương gia thường được sử dụng quầy làm thủ tục riêng, ưu tiên lên máy bay và nhận hành lý sớm hơn sau khi hạ cánh. Một số hãng còn cung cấp lối ưu tiên tại khu vực kiểm tra an ninh, giúp tiết kiệm thời gian chờ đợi.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_tg_1));
        sections.add(createSection(2, "Không gian ghế ngồi riêng tư và thoải mái", "Ghế hạng Thương gia thường có kích thước rộng, khoảng cách để chân thoải mái và khả năng điều chỉnh linh hoạt. Trên một số chuyến bay đường dài, ghế có thể ngả thành giường nằm, đi kèm không gian riêng tư và khu vực làm việc tiện lợi.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_tg_2));
        sections.add(createSection(3, "Dịch vụ và ẩm thực được nâng tầm", "Trải nghiệm hạng Thương gia thường đi kèm phong cách phục vụ chu đáo, thực đơn đa dạng và cách trình bày chỉn chu hơn. Hành khách có thể được lựa chọn suất ăn, đồ uống hoặc đăng ký trước các yêu cầu ăn uống đặc biệt.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_tg_3));
        sections.add(createSection(4, "Hành lý linh hoạt và phòng chờ tiện nghi", "Vé hạng Thương gia thường có hạn mức hành lý cao hơn. Bên cạnh đó, một số loại vé còn đi kèm quyền sử dụng phòng chờ sân bay với không gian nghỉ ngơi, Wi-Fi, đồ ăn nhẹ và khu vực làm việc yên tĩnh.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_tg_4));
        sections.add(createSection(5, "Dễ dàng so sánh vé hạng Thương gia trên Skyline", "Trên Skyline, bạn có thể tìm kiếm và so sánh nhiều chuyến bay dựa trên giá vé, thời gian khởi hành, hạn mức hành lý và các điều kiện đi kèm. Nhờ đó, bạn có thể tận hưởng trải nghiệm hạng Thương gia một cách trọn vẹn nhất.", "android.resource://" + getPackageName() + "/" + R.drawable.blog_tg_5));
        b.sections = sections;

        Blog.CTA cta = new Blog.CTA();
        cta.text = "ĐẶT VÉ NGAY";
        cta.action = "BOOK_FLIGHT";
        b.cta = cta;
        return b;
    }

    private Blog.Section createSection(int num, String title, String desc, String img) {
        Blog.Section s = new Blog.Section();
        s.sectionNumber = num;
        s.title = title;
        s.type = "text";
        Blog.SectionItem item = new Blog.SectionItem();
        item.description = desc;
        item.imageUrl = img;
        s.items = new ArrayList<>();
        s.items.add(item);
        return s;
    }

    private void openBlogDetail(Blog blog) {
        Intent intent = new Intent(this, BlogDetailActivity.class);
        intent.putExtra("blog", blog);
        startActivity(intent);
    }
}
