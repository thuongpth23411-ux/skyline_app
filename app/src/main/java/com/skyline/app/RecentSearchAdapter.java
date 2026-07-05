package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.network.RecentSearch;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    private List<RecentSearch> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RecentSearch item);
    }

    public RecentSearchAdapter(List<RecentSearch> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentSearch item = list.get(position);
        holder.tvRoute.setText(item.fromAirportId + " - " + item.toAirportId);
        
        String dateText = item.departureDate;
        if (item.isRoundTrip && item.returnDate != null) {
            dateText += " - " + item.returnDate;
        }
        holder.tvDate.setText(dateText);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoute, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvRecentRoute);
            tvDate = itemView.findViewById(R.id.tvRecentDate);
        }
    }
}
