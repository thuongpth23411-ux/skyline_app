package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.network.Promotion;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private final List<Promotion> vouchers;
    private final OnVoucherSelectedListener listener;
    private final double currentSubTotal;
    private final String userRank;
    private int selectedPosition = -1;
    private int bestChoicePosition = -1;

    private static final Map<String, Integer> rankWeights = new HashMap<>();
    static {
        rankWeights.put("NONE", 0);
        rankWeights.put("Đồng", 1);
        rankWeights.put("Bạc", 2);
        rankWeights.put("Vàng", 3);
        rankWeights.put("Kim cương", 4);
    }

    public interface OnVoucherSelectedListener {
        void onSelected(Promotion promotion);
        void onConditionClick(Promotion promotion);
    }

    public VoucherAdapter(List<Promotion> vouchers, String currentCode, double subTotal, String userRank, OnVoucherSelectedListener listener) {
        this.vouchers = vouchers;
        this.currentSubTotal = subTotal;
        this.userRank = userRank != null ? userRank : "NONE";
        this.listener = listener;
        
        // Sắp xếp voucher: Cái dùng được lên đầu, giảm nhiều tiền hơn xếp trên
        this.vouchers.sort((v1, v2) -> {
            boolean e1 = isEligible(v1);
            boolean e2 = isEligible(v2);
            if (e1 != e2) return e1 ? -1 : 1;
            
            double d1 = calculatePotentialDiscount(v1);
            double d2 = calculatePotentialDiscount(v2);
            return Double.compare(d2, d1); // Giảm nhiều hơn lên trên
        });
        
        calculateBestChoiceAndSelection(currentCode);
    }

    private void calculateBestChoiceAndSelection(String currentCode) {
        double maxDiscount = -1;
        for (int i = 0; i < vouchers.size(); i++) {
            Promotion v = vouchers.get(i);
            
            if (currentCode != null && v.getCode().equalsIgnoreCase(currentCode)) {
                selectedPosition = i;
            }

            if (isEligible(v)) {
                double discount = calculatePotentialDiscount(v);
                if (discount > maxDiscount) {
                    maxDiscount = discount;
                    bestChoicePosition = i;
                }
            }
        }
    }

    private boolean isEligible(Promotion v) {
        if (!"Active".equalsIgnoreCase(v.getStatus())) return false;
        if (v.getQuantity() <= 0) return false;
        if (currentSubTotal < v.getMinimumOrder()) return false;
        
        // --- RANK ELIGIBILITY LOGIC ---
        String promoCategory = v.getCategory(); // e.g. "BẠC", "VÀNG", "MEMBER"
        
        if (promoCategory != null && !promoCategory.isEmpty()) {
            if ("MEMBER".equalsIgnoreCase(promoCategory)) {
                return !userRank.equals("NONE");
            }
            
            // Check if category is a rank name
            Integer promoRankWeight = getRankWeight(promoCategory);
            if (promoRankWeight > 0) {
                Integer userRankWeight = getRankWeight(userRank);
                // User must have rank equal to or higher than the promo rank
                return userRankWeight >= promoRankWeight;
            }
        }
        
        return true;
    }

    private Integer getRankWeight(String rankName) {
        if (rankName == null) return 0;
        for (String key : rankWeights.keySet()) {
            if (key.equalsIgnoreCase(rankName)) return rankWeights.get(key);
        }
        return 0;
    }

    private double calculatePotentialDiscount(Promotion v) {
        double discount;
        if ("FIXED".equalsIgnoreCase(v.getDiscountType())) {
            discount = v.getDiscountValue();
        } else {
            discount = currentSubTotal * (v.getDiscountValue() / 100.0);
            if (v.getMaxDiscount() > 0 && discount > v.getMaxDiscount()) {
                discount = v.getMaxDiscount();
            }
        }
        return discount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion v = vouchers.get(position);
        holder.tvTitle.setText(v.getTitle());
        
        DecimalFormat df = new DecimalFormat("#,###");
        holder.tvMinOrder.setText("Đơn tối thiểu " + df.format(v.getMinimumOrder()) + "đ");
        
        boolean eligible = isEligible(v);
        String statusText = "HSD: " + v.getExpiryDate();
        
        if (v.getQuantity() <= 0) {
            statusText = "Đã hết lượt sử dụng";
        } else if (!"Active".equalsIgnoreCase(v.getStatus())) {
            statusText = "Voucher không khả dụng";
        } else if (currentSubTotal < v.getMinimumOrder()) {
            statusText = "Chưa đạt mức tối thiểu";
        } else {
            // Rank specific message
            Integer promoRankWeight = getRankWeight(v.getCategory());
            if (promoRankWeight > getRankWeight(userRank)) {
                statusText = "Dành cho hạng " + v.getCategory() + " trở lên";
            }
        }

        holder.tvExpiry.setText(statusText);
        holder.tvExpiry.setTextColor(eligible ? 0xFF718096 : 0xFFFF4D2D);
        
        float alpha = eligible ? 1.0f : 0.5f;
        holder.itemView.setAlpha(alpha);
        holder.layoutLeft.setBackgroundTintList(android.content.res.ColorStateList.valueOf(eligible ? 
                ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_teal) : 
                android.graphics.Color.parseColor("#CBD5E0")));

        holder.rbSelect.setChecked(position == selectedPosition);
        holder.rbSelect.setEnabled(eligible);
        holder.tvBestChoice.setVisibility(position == bestChoicePosition && eligible ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(view -> {
            if (!eligible) {
                String reason = getIneligibleReason(v);
                Toast.makeText(view.getContext(), reason, Toast.LENGTH_SHORT).show();
                return;
            }
            
            int old = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selectedPosition);
            listener.onSelected(v);
        });

        holder.tvCondition.setOnClickListener(v1 -> listener.onConditionClick(v));
    }

    public void updateSelection(String code) {
        int old = selectedPosition;
        selectedPosition = -1;
        for (int i = 0; i < vouchers.size(); i++) {
            if (vouchers.get(i).getCode().equalsIgnoreCase(code)) {
                selectedPosition = i;
                break;
            }
        }
        notifyItemChanged(old);
        if (selectedPosition != -1) notifyItemChanged(selectedPosition);
    }

    private String getIneligibleReason(Promotion v) {
        if (v.getQuantity() <= 0) return "Voucher này đã hết lượt sử dụng!";
        if (!"Active".equalsIgnoreCase(v.getStatus())) return "Voucher hiện không khả dụng!";
        if (currentSubTotal < v.getMinimumOrder()) return "Đơn hàng chưa đạt mức tối thiểu!";
        
        Integer promoRankWeight = getRankWeight(v.getCategory());
        if (promoRankWeight > getRankWeight(userRank)) {
            if (userRank.equals("NONE")) return "Vui lòng đăng nhập để sử dụng ưu đãi!";
            return "Hạng của bạn chưa đủ để sử dụng mã này!";
        }

        return "Bạn không đủ điều kiện sử dụng mã này!";
    }

    @Override
    public int getItemCount() {
        return vouchers != null ? vouchers.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMinOrder, tvExpiry, tvBestChoice, tvCondition;
        CheckBox rbSelect;
        View layoutLeft;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvMinOrder = itemView.findViewById(R.id.tvVoucherMinOrder);
            tvExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
            rbSelect = itemView.findViewById(R.id.rbSelect);
            tvBestChoice = itemView.findViewById(R.id.tvBestChoice);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
        }
    }
}
