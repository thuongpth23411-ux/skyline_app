package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.skyline.app.databinding.ItemPromotionBinding;
import com.skyline.model.Promotion;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {
    private List<Promotion> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Promotion item);
    }

    public PromotionAdapter(List<Promotion> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPromotionBinding binding = ItemPromotionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PromotionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class PromotionViewHolder extends RecyclerView.ViewHolder {
        private ItemPromotionBinding binding;

        public PromotionViewHolder(ItemPromotionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Promotion item) {
            String url = item.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(binding.imgPromo.getContext())
                        .load(url)
                        .placeholder(R.drawable.img_brand_banner)
                        .error(R.drawable.img_brand_banner)
                        .into(binding.imgPromo);
            } else if (item.getImageRes() != 0) {
                binding.imgPromo.setImageResource(item.getImageRes());
            } else {
                binding.imgPromo.setImageResource(R.drawable.img_brand_banner);
            }

            binding.tvPromoTitle.setText(item.getTitle());
            binding.tvPromoDate.setText(item.getDate());
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
