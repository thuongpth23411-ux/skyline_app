package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.network.Airport;
import java.util.List;

public class AirportAdapter extends RecyclerView.Adapter<AirportAdapter.ViewHolder> {

    private List<Airport> airports;
    private OnAirportClickListener listener;

    public interface OnAirportClickListener {
        void onAirportClick(Airport airport);
    }

    public AirportAdapter(List<Airport> airports, OnAirportClickListener listener) {
        this.airports = airports;
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
        holder.tvName.setText(airport.getName());
        holder.tvLocation.setText(airport.getCity() + ", " + airport.getCountry());
        holder.tvCode.setText(airport.getCode());
        holder.itemView.setOnClickListener(v -> listener.onAirportClick(airport));
    }

    @Override
    public int getItemCount() {
        return airports.size();
    }

    public void updateList(List<Airport> newList) {
        this.airports = newList;
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
