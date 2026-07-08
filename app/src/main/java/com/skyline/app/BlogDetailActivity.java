package com.skyline.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.skyline.app.databinding.ActivityBlogDetailBinding;
import com.skyline.app.databinding.ItemBlogQuickInfoBinding;
import com.skyline.app.databinding.ItemBlogSectionFoodBinding;
import com.skyline.app.databinding.ItemBlogSectionPlaceBinding;
import com.skyline.app.databinding.ItemBlogSectionReasonBinding;
import com.skyline.app.databinding.ItemBlogSectionTipBinding;
import com.skyline.app.network.Blog;
import com.skyline.app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlogDetailActivity extends AppCompatActivity {

    private ActivityBlogDetailBinding binding;
    private Blog blog;
    private final List<View> sectionViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlogDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        blog = (Blog) getIntent().getSerializableExtra("blog");
        if (blog == null) {
            String slug = getIntent().getStringExtra("slug");
            if (slug != null) loadBlog(slug);
            else finish();
        } else {
            setupUI();
        }

        // Custom back button action
        binding.btnBackCustom.setOnClickListener(v -> finish());
    }

    private void loadBlog(String identifier) {
        RetrofitClient.getInstance().getBlogByIdentifier(identifier).enqueue(new Callback<Blog>() {
            @Override
            public void onResponse(Call<Blog> call, Response<Blog> response) {
                if (response.isSuccessful() && response.body() != null) {
                    blog = response.body();
                    setupUI();
                } else {
                    Toast.makeText(BlogDetailActivity.this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onFailure(Call<Blog> call, Throwable t) {
                Toast.makeText(BlogDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupUI() {
        Glide.with(this).load(blog.coverImageUrl).placeholder(R.drawable.img_destination_danang).into(binding.ivCover);
        binding.tvTitle.setText(blog.title);
        binding.tvCategory.setText(blog.category != null ? blog.category.toUpperCase() : "ĐIỂM ĐẾN");
        binding.tvAuthorName.setText(blog.author != null ? blog.author.name : "Skyline Team");
        binding.tvMeta.setText(String.format("%s • %s", formatDate(blog.publishedDate), blog.readTime));
        binding.tvSummary.setText(blog.introContent != null ? blog.introContent : blog.shortDescription);

        if (blog.author != null && blog.author.avatarUrl != null) {
            Glide.with(this).load(blog.author.avatarUrl).placeholder(R.drawable.ic_profile).into(binding.ivAuthorAvatar);
        }

        setupQuickInfo();
        setupSections(); 
        setupToc();

        binding.btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, blog.title);
            share.putExtra(Intent.EXTRA_TEXT, blog.title + "\nKhám phá ngay tại Skyline App!");
            startActivity(Intent.createChooser(share, "Chia sẻ bài viết"));
        });

        binding.btnSave.setOnClickListener(v -> Toast.makeText(this, "Đã lưu vào danh sách yêu thích", Toast.LENGTH_SHORT).show());

        if (blog.cta != null) {
            binding.btnBookNow.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "BOOK");
                intent.putExtra("DESTINATION_CODE", blog.cta.destinationCode);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }

    private void setupQuickInfo() {
        binding.gridQuickInfo.removeAllViews();
        if (blog.quickInfos == null) return;
        
        for (Blog.QuickInfo info : blog.quickInfos) {
            ItemBlogQuickInfoBinding infoBinding = ItemBlogQuickInfoBinding.inflate(getLayoutInflater(), binding.gridQuickInfo, false);
            infoBinding.tvTitle.setText(info.title);
            infoBinding.tvValue.setText(info.value);
            
            int iconRes = R.drawable.ic_location;
            if ("calendar".equals(info.icon)) iconRes = R.drawable.ic_calendar;
            else if ("temperature".equals(info.icon)) iconRes = R.drawable.ic_trending_up;
            else if ("beach".equals(info.icon)) iconRes = R.drawable.ic_ticket; // Better icon for experiences
            
            infoBinding.ivIcon.setImageResource(iconRes);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.setMargins(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12)); // Increased vertical margin
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            infoBinding.getRoot().setLayoutParams(params);
            
            binding.gridQuickInfo.addView(infoBinding.getRoot());
        }
    }

    private void setupToc() {
        if (blog.sections == null || blog.sections.isEmpty()) return;
        View tocView = getLayoutInflater().inflate(R.layout.item_blog_toc, binding.sectionsContainer, false);
        LinearLayout llItems = tocView.findViewById(R.id.llTocItems);
        for (int i = 0; i < blog.sections.size(); i++) {
            final int index = i;
            Blog.Section section = blog.sections.get(i);
            TextView tv = new TextView(this);
            tv.setText(String.format(Locale.getDefault(), "%d. %s", i + 1, section.title));
            tv.setTextColor(Color.parseColor("#0B4DA2"));
            tv.setTextSize(15);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setPadding(0, 0, 0, dpToPx(14));
            tv.setLineSpacing(0, 1.2f);
            tv.setOnClickListener(v -> {
                if (index < sectionViews.size()) {
                    View target = sectionViews.get(index);
                    binding.nestedScrollView.post(() -> binding.nestedScrollView.smoothScrollTo(0, target.getTop()));
                }
            });
            llItems.addView(tv);
        }
        binding.sectionsContainer.addView(tocView, 0);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupSections() {
        binding.sectionsContainer.removeAllViews();
        sectionViews.clear();
        if (blog.sections == null) return;

        for (Blog.Section section : blog.sections) {
            View headerView = getLayoutInflater().inflate(R.layout.item_blog_section_header, binding.sectionsContainer, false);
            ((TextView)headerView.findViewById(R.id.tvSectionNumber)).setText(String.valueOf(section.sectionNumber));
            ((TextView)headerView.findViewById(R.id.tvSectionTitle)).setText(section.title);
            binding.sectionsContainer.addView(headerView);
            sectionViews.add(headerView);

            if (section.items == null) continue;

            if ("food_grid".equals(section.type) || "reason_grid".equals(section.type)) {
                GridLayout grid = new GridLayout(this);
                grid.setColumnCount(2);
                grid.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                
                for (Blog.SectionItem item : section.items) {
                    View itemView;
                    if ("food_grid".equals(section.type)) {
                        ItemBlogSectionFoodBinding foodBinding = ItemBlogSectionFoodBinding.inflate(getLayoutInflater(), grid, false);
                        foodBinding.tvTitle.setText(item.title);
                        Glide.with(this).load(item.imageUrl).placeholder(R.drawable.bg_square_placeholder).into(foodBinding.ivImage);
                        itemView = foodBinding.getRoot();
                    } else {
                        ItemBlogSectionReasonBinding reasonBinding = ItemBlogSectionReasonBinding.inflate(getLayoutInflater(), grid, false);
                        reasonBinding.tvTitle.setText(item.title);
                        reasonBinding.tvDescription.setText(item.description);
                        int iconRes = R.drawable.ic_priority;
                        int color = Color.parseColor("#0B4DA2");
                        if ("leaf".equals(item.icon)) { iconRes = R.drawable.ic_sparkle; color = Color.parseColor("#16A34A"); }
                        else if ("users".equals(item.icon)) { iconRes = R.drawable.ic_profile; color = Color.parseColor("#9333EA"); }
                        else if ("building".equals(item.icon)) { iconRes = R.drawable.ic_building; color = Color.parseColor("#EA580C"); }
                        else if ("heart".equals(item.icon)) { iconRes = R.drawable.ic_heart; color = Color.parseColor("#E11D48"); }
                        reasonBinding.ivIcon.setImageResource(iconRes);
                        reasonBinding.ivIcon.setImageTintList(ColorStateList.valueOf(color));
                        itemView = reasonBinding.getRoot();
                    }
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setGravity(android.view.Gravity.FILL);
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    itemView.setLayoutParams(params);
                    grid.addView(itemView);
                }
                binding.sectionsContainer.addView(grid);
            } else {
                for (Blog.SectionItem item : section.items) {
                    if ("tips".equals(section.type)) {
                        ItemBlogSectionTipBinding tipBinding = ItemBlogSectionTipBinding.inflate(getLayoutInflater(), binding.sectionsContainer, false);
                        String fullText = item.title + ": " + item.description;
                        SpannableStringBuilder ssb = new SpannableStringBuilder(fullText);
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, item.title.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tipBinding.tvContent.setText(ssb);
                        binding.sectionsContainer.addView(tipBinding.getRoot());
                    } else if ("place_grid".equals(section.type)) {
                        ItemBlogSectionPlaceBinding placeBinding = ItemBlogSectionPlaceBinding.inflate(getLayoutInflater(), binding.sectionsContainer, false);
                        placeBinding.tvTitle.setText(item.title);
                        placeBinding.tvSubtitle.setText(item.subtitle);
                        placeBinding.tvDescription.setText(item.description);
                        Glide.with(this).load(item.imageUrl).placeholder(R.drawable.bg_square_placeholder).into(placeBinding.ivImage);
                        binding.sectionsContainer.addView(placeBinding.getRoot());
                    } else if ("itinerary".equals(section.type)) {
                        View itView = getLayoutInflater().inflate(R.layout.item_blog_section_itinerary, binding.sectionsContainer, false);
                        ((TextView)itView.findViewById(R.id.tvDayTitle)).setText(item.title);
                        if (item.bulletPoints != null) {
                            StringBuilder sb = new StringBuilder();
                            for (String p : item.bulletPoints) sb.append("• ").append(p).append("\n");
                            ((TextView)itView.findViewById(R.id.tvPoints)).setText(sb.toString().trim());
                        }
                        binding.sectionsContainer.addView(itView);
                    }
                }
            }
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "20.05.2026";
        try {
            String datePart = isoDate.split("T")[0];
            String[] parts = datePart.split("-");
            return parts[2] + "." + parts[1] + "." + parts[0];
        } catch (Exception e) { return "20.05.2026"; }
    }
}
