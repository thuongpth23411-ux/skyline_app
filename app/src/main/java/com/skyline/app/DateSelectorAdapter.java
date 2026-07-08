package com.skyline.app;

import android.graphics.Color;
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
        public long minPrice; 
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
        void onDateSelected(Date date, int position);
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
            holder.tvPrice.setVisibility(View.VISIBLE);
        } else {
            // Hide the price if it's 0 or -1, as requested (don't show "Hết vé")
            holder.tvPrice.setVisibility(View.INVISIBLE);
        }

        if (item.isSelected) {
            holder.rootLayout.setBackgroundColor(Color.WHITE);
            holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_blue_dark));
            holder.tvDate.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_blue_dark));
            holder.tvPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_blue_dark));
            holder.indicator.setVisibility(View.VISIBLE);
            holder.tvDate.setTextSize(20);
        } else {
            holder.rootLayout.setBackgroundColor(Color.parseColor("#F3F4F6"));
            holder.tvDayOfWeek.setTextColor(Color.parseColor("#9CA3AF"));
            holder.tvDate.setTextColor(Color.parseColor("#4B5563"));
            holder.tvPrice.setTextColor(Color.parseColor("#9CA3AF"));
            holder.indicator.setVisibility(View.INVISIBLE);
            holder.tvDate.setTextSize(18);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!item.isSelected) {
                updateSelection(position);
                listener.onDateSelected(item.date, position);
            }
        });
    }

    public void updateSelection(int position) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).isSelected = (i == position);
        }
        notifyDataSetChanged();
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
        View rootLayout, indicator;
        TextView tvDayOfWeek, tvDate, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            indicator = itemView.findViewById(R.id.indicator);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
