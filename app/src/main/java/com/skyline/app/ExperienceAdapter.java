package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemExperienceBinding;
import com.skyline.model.Experience;
import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder> {
    private List<Experience> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Experience item);
    }

    public ExperienceAdapter(List<Experience> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExperienceBinding binding = ItemExperienceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExperienceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ExperienceViewHolder extends RecyclerView.ViewHolder {
        private ItemExperienceBinding binding;

        public ExperienceViewHolder(ItemExperienceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Experience item) {
            binding.imgExperience.setImageResource(item.getImageRes());
            binding.tvExperienceTag.setText(item.getTag());
            binding.tvExperienceTitle.setText(item.getTitle());
            binding.tvExperienceDesc.setText(item.getDescription());
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
