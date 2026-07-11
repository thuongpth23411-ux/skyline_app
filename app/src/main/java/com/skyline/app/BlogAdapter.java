package com.skyline.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.skyline.app.databinding.ItemBlogLatestBinding;
import com.skyline.app.network.Blog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {

    private List<Blog> blogs;
    private OnBlogClickListener listener;
    private final Set<String> favoriteBlogIds = new HashSet<>();

    public interface OnBlogClickListener {
        void onBlogClick(Blog blog);
    }

    public BlogAdapter(List<Blog> blogs, OnBlogClickListener listener) {
        this.blogs = blogs;
        this.listener = listener;
    }

    public void updateData(List<Blog> newBlogs) {
        this.blogs = newBlogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBlogLatestBinding binding = ItemBlogLatestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Blog blog = blogs.get(position);
        holder.binding.tvTitle.setText(blog.title);
        holder.binding.tvCategory.setText(blog.category != null ? blog.category.toUpperCase() : "ĐIỂM ĐẾN");
        holder.binding.tvDescription.setText(blog.shortDescription);
        holder.binding.tvReadTime.setText(blog.readTime);
        holder.binding.tvDate.setText(formatDate(blog.publishedDate));

        Glide.with(holder.itemView.getContext())
                .load(blog.getFullThumbnailUrl())
                .placeholder(R.drawable.img_destination_danang)
                .into(holder.binding.ivThumbnail);

        // Favorite Logic
        boolean isFav = favoriteBlogIds.contains(blog.blogCode);
        updateFavoriteUI(holder, isFav);

        holder.binding.btnSave.setOnClickListener(v -> {
            boolean currentFav = favoriteBlogIds.contains(blog.blogCode);
            if (currentFav) {
                favoriteBlogIds.remove(blog.blogCode);
                Toast.makeText(holder.itemView.getContext(), "Đã xóa khỏi mục yêu thích", Toast.LENGTH_SHORT).show();
                updateFavoriteUI(holder, false);
            } else {
                favoriteBlogIds.add(blog.blogCode);
                Toast.makeText(holder.itemView.getContext(), "Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
                updateFavoriteUI(holder, true);
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onBlogClick(blog));
    }

    private void updateFavoriteUI(ViewHolder holder, boolean isFav) {
        if (isFav) {
            holder.binding.btnSave.setImageTintList(ColorStateList.valueOf(Color.parseColor("#E11D48"))); // Red
        } else {
            holder.binding.btnSave.setImageTintList(ColorStateList.valueOf(Color.parseColor("#9CA3AF"))); // Gray
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "01/01/2026";
        try {
            String datePart = isoDate.split("T")[0];
            String[] parts = datePart.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return "12/06/2026";
        }
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemBlogLatestBinding binding;
        public ViewHolder(ItemBlogLatestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
