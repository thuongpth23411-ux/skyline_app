package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.network.Airport;
import java.util.ArrayList;
import java.util.List;

public class AirportAdapter extends RecyclerView.Adapter<AirportAdapter.ViewHolder> {

    private List<Airport> airports;
    private OnAirportClickListener listener;

    public interface OnAirportClickListener {
        void onAirportClick(Airport airport);
    }

    public AirportAdapter(List<Airport> airports, OnAirportClickListener listener) {
        this.airports = new ArrayList<>(airports);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_airport, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Airport airport = airports.get(position);
        
        String name = airport.getName() != null ? airport.getName() : "Không tên";
        String city = airport.getCity() != null ? airport.getCity() : "";
        String country = airport.getCountry() != null ? airport.getCountry() : "";
        String code = airport.getCode() != null ? airport.getCode() : "???";

        holder.tvName.setText(name);
        holder.tvLocation.setText(city + (city.isEmpty() || country.isEmpty() ? "" : ", ") + country);
        holder.tvCode.setText(code);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAirportClick(airport);
        });
    }

    @Override
    public int getItemCount() {
        return airports != null ? airports.size() : 0;
    }

    public void updateList(List<Airport> newList) {
        if (newList == null) return;
        this.airports = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCode = itemView.findViewById(R.id.tvCode);
        }
    }
}
