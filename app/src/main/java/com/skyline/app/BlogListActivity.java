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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlogListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActions();
        setupRecyclerViews();
        loadBlogs();
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());

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
                    List<Blog> allBlogs = response.body();
                    List<Blog> featured = new ArrayList<>();
                    List<Blog> latest = new ArrayList<>();
                    
                    for (Blog b : allBlogs) {
                        if (b.isFeatured) featured.add(b);
                        latest.add(b);
                    }
                    
                    featuredAdapter.updateData(featured);
                    latestAdapter.updateData(latest);
                }
            }

            @Override
            public void onFailure(Call<List<Blog>> call, Throwable t) {
                Toast.makeText(BlogListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openBlogDetail(Blog blog) {
        Intent intent = new Intent(this, BlogDetailActivity.class);
        intent.putExtra("blog", blog);
        startActivity(intent);
    }
}
