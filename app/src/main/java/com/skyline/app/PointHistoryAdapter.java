package com.skyline.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemPointHistoryBinding;
import com.skyline.app.network.PointHistory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.ViewHolder> {

    private List<PointHistory> historyList;

    public PointHistoryAdapter(List<PointHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPointHistoryBinding binding = ItemPointHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PointHistory item = historyList.get(position);
        holder.binding.tvDescription.setText(item.getDescription());
        
        // Format date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(item.getDate());
            if (date != null) holder.binding.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.binding.tvDate.setText(item.getDate());
        }

        DecimalFormat df = new DecimalFormat("#,###");
        if (item.getAmount() > 0) {
            holder.binding.tvAmount.setVisibility(View.VISIBLE);
            holder.binding.tvAmount.setText("Số tiền: " + df.format(item.getAmount()) + " VND");
        } else {
            holder.binding.tvAmount.setVisibility(View.GONE);
        }

        // Logic for EARN / REDEEM / REVOKE
        if ("EARN".equalsIgnoreCase(item.getType())) {
            holder.binding.tvPoints.setText("+" + item.getPoints());
            holder.binding.tvPoints.setTextColor(Color.parseColor("#22C55E")); // Green
            holder.binding.ivTypeIcon.setImageResource(R.drawable.ic_check_auth);
            holder.binding.ivTypeIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#22C55E")));
            holder.binding.cardIcon.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
        } else {
            // REDEEM or REVOKE
            holder.binding.tvPoints.setText("-" + item.getPoints());
            holder.binding.tvPoints.setTextColor(Color.parseColor("#DC2626")); // Red
            holder.binding.ivTypeIcon.setImageResource(R.drawable.ic_cancel);
            holder.binding.ivTypeIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
            holder.binding.cardIcon.setCardBackgroundColor(Color.parseColor("#FEF2F2"));
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<PointHistory> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPointHistoryBinding binding;
        ViewHolder(ItemPointHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
