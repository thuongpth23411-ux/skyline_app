package com.skyline.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemPromotionFullBinding;
import com.skyline.app.network.Promotion;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FullPromotionAdapter extends RecyclerView.Adapter<FullPromotionAdapter.FullPromotionViewHolder> {
    private List<Promotion> items;
    private Set<String> savedVoucherIds = new HashSet<>();
    private final OnPromotionClickListener listener;

    public interface OnPromotionClickListener {
        void onCopyCode(String code);
        void onSaveVoucher(Promotion item);
        void onItemClick(Promotion item);
    }

    public FullPromotionAdapter(List<Promotion> items, OnPromotionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Promotion> newItems, Set<String> savedIds) {
        this.items = newItems;
        this.savedVoucherIds = savedIds != null ? savedIds : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FullPromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPromotionFullBinding binding = ItemPromotionFullBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FullPromotionViewHolder(binding, listener, savedVoucherIds);
    }

    @Override
    public void onBindViewHolder(@NonNull FullPromotionViewHolder holder, int position) {
        if (items != null && position < items.size()) {
            holder.bind(items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class FullPromotionViewHolder extends RecyclerView.ViewHolder {
        private final ItemPromotionFullBinding binding;
        private final OnPromotionClickListener listener;
        private final Set<String> savedVoucherIds;

        public FullPromotionViewHolder(ItemPromotionFullBinding binding, OnPromotionClickListener listener, Set<String> savedVoucherIds) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.savedVoucherIds = savedVoucherIds;
        }

        public void bind(Promotion item) {
            if (item == null) return;

            binding.tvTitle.setText(item.getTitle());
            binding.tvDesc.setText(item.getDescription());
            
            String category = item.getCategory() != null ? item.getCategory().trim().toUpperCase() : "";
            String displayCategory = category;
            
            if (category.equals("MEMBER")) displayCategory = "THÀNH VIÊN";
            else if (category.equals("EXCLUSIVE")) displayCategory = "ĐỘC QUYỀN";
            else if (category.equals("PAYMENT")) displayCategory = "THANH TOÁN";
            else if (category.equals("NEW_USER")) displayCategory = "KHÁCH HÀNG MỚI";
            
            binding.tvCategory.setText(displayCategory);
            binding.tvExpiry.setText("Đến " + (item.getExpiryDate().isEmpty() ? "vô thời hạn" : item.getExpiryDate()));
            binding.tvValue.setText(item.getValue().isEmpty() ? "Quà tặng" : item.getValue());
            binding.tvCodeLabel.setText("MÃ: " + (item.getCode().isEmpty() ? "---" : item.getCode()));

            // Màu sắc và ảnh mặc định theo danh mục chuẩn
            int placeholderRes = R.drawable.img_brand_banner;
            int categoryColor = 0xFF4A658A;
            if (category.equals("MEMBER")) {
                categoryColor = 0xFF00796B;
                placeholderRes = R.drawable.img_experience_first;
            } else if (category.equals("EXCLUSIVE")) {
                categoryColor = 0xFFBF953F;
                placeholderRes = R.drawable.bg_member_card_gradient;
            } else if (category.equals("NEW_USER")) {
                categoryColor = 0xFF3F51B5;
                placeholderRes = R.drawable.img_experience_economy;
            }

            // Tải ảnh từ URL MongoDB bằng Glide
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("/")) {
                    imageUrl = "http://10.0.2.2:3000" + imageUrl;
                }
                
                com.bumptech.glide.Glide.with(binding.imgPromo.getContext())
                        .load(imageUrl)
                        .placeholder(placeholderRes)
                        .error(placeholderRes)
                        .centerCrop()
                        .into(binding.imgPromo);
            } else {
                binding.imgPromo.setImageResource(placeholderRes);
            }

            if (binding.tvCategory.getBackground() != null) {
                binding.tvCategory.getBackground().setTint(categoryColor);
            }

            boolean isSaved = savedVoucherIds != null && savedVoucherIds.contains(item.getId());
            if (isSaved) {
                binding.btnSave.setImageResource(R.drawable.ic_bookmark);
                binding.btnSave.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            } else {
                binding.btnSave.setImageResource(R.drawable.ic_bookmark_border);
                binding.btnSave.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            }

            binding.btnCopy.setOnClickListener(v -> listener.onCopyCode(item.getCode()));
            binding.btnSave.setOnClickListener(v -> listener.onSaveVoucher(item));
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
