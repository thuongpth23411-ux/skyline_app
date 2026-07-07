package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateSelectorAdapter extends RecyclerView.Adapter<DateSelectorAdapter.ViewHolder> {

    public static class DateItem {
        public Date date;
        public long minPrice; // -1 means no data, 0 means no flights
        public boolean isSelected;

        public DateItem(Date date, long minPrice, boolean isSelected) {
            this.date = date;
            this.minPrice = minPrice;
            this.isSelected = isSelected;
        }
    }

    private List<DateItem> items;
    private OnDateSelectedListener listener;
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private DecimalFormat priceFormat = new DecimalFormat("#,###");

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public DateSelectorAdapter(List<DateItem> items, OnDateSelectedListener listener) {
        this.items = items;
        this.listener = listener;
        dayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DateItem item = items.get(position);

        holder.tvDayOfWeek.setText(capitalize(dayFormat.format(item.date)));
        holder.tvDate.setText(dateFormat.format(item.date));

        if (item.minPrice > 0) {
            holder.tvPrice.setText("từ " + priceFormat.format(item.minPrice) + " VND");
        } else if (item.minPrice == 0) {
            holder.tvPrice.setText("Hết vé");
        } else {
            holder.tvPrice.setText("..."); // Đang tải hoặc chưa có dữ liệu
        }

        int activeColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_blue_dark);
        int inactiveColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_text_secondary);

        if (item.isSelected) {
            holder.tvDayOfWeek.setTextColor(activeColor);
            holder.tvDate.setTextColor(activeColor);
            holder.tvPrice.setTextColor(activeColor);
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvDayOfWeek.setTextColor(inactiveColor);
            holder.tvDate.setTextColor(inactiveColor);
            holder.tvPrice.setTextColor(inactiveColor);
            holder.indicator.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!item.isSelected) {
                for (int i = 0; i < items.size(); i++) {
                    items.get(i).isSelected = (i == position);
                }
                notifyDataSetChanged();
                listener.onDateSelected(item.date);
            }
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfWeek, tvDate, tvPrice;
        View indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }
}
