package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.skyline.app.databinding.ItemBlogFeaturedBinding;
import com.skyline.app.network.Blog;
import java.util.List;

public class FeaturedBlogAdapter extends RecyclerView.Adapter<FeaturedBlogAdapter.ViewHolder> {

    private List<Blog> blogs;
    private OnBlogClickListener listener;

    public interface OnBlogClickListener {
        void onBlogClick(Blog blog);
    }

    public FeaturedBlogAdapter(List<Blog> blogs, OnBlogClickListener listener) {
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
        ItemBlogFeaturedBinding binding = ItemBlogFeaturedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Blog blog = blogs.get(position);
        holder.binding.tvTitle.setText(blog.title);
        holder.binding.tvDestination.setText(blog.destination != null ? blog.destination : "Việt Nam");
        holder.binding.tvReadTime.setText(blog.readTime);

        Glide.with(holder.itemView.getContext())
                .load(blog.thumbnailUrl)
                .placeholder(R.drawable.img_destination_danang)
                .into(holder.binding.ivThumbnail);

        holder.itemView.setOnClickListener(v -> listener.onBlogClick(blog));
    }

    @Override
    public int getItemCount() {
        return blogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemBlogFeaturedBinding binding;
        public ViewHolder(ItemBlogFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
