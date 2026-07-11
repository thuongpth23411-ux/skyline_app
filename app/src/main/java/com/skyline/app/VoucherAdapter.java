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
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private final List<Promotion> vouchers;
    private final OnVoucherSelectedListener listener;
    private final double currentSubTotal;
    private final String userRank;
    private int selectedPosition = -1;
    private int bestChoicePosition = -1;

    public interface OnVoucherSelectedListener {
        void onSelected(Promotion promotion);
        void onConditionClick(Promotion promotion);
    }

    public VoucherAdapter(List<Promotion> vouchers, String currentCode, double subTotal, String userRank, OnVoucherSelectedListener listener) {
        this.vouchers = vouchers;
        this.currentSubTotal = subTotal;
        this.userRank = userRank != null ? userRank : "NONE";
        this.listener = listener;
        
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
        
        if ("MEMBER".equalsIgnoreCase(v.getCategory())) {
            if (userRank.equals("NONE")) return false;
        }
        return true;
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
        } else if ("MEMBER".equalsIgnoreCase(v.getCategory()) && userRank.equals("NONE")) {
            statusText = "Dành riêng cho hội viên";
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
        if ("MEMBER".equalsIgnoreCase(v.getCategory()) && userRank.equals("NONE")) return "Vui lòng đăng nhập để sử dụng ưu đãi hội viên!";
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
