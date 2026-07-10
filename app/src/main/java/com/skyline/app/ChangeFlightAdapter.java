package com.skyline.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.network.Flight;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChangeFlightAdapter extends RecyclerView.Adapter<ChangeFlightAdapter.ViewHolder> {

    private List<Flight> flights;
    private double oldPrice;
    private OnFlightClickListener listener;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private DecimalFormat priceFormat = new DecimalFormat("#,###");

    public interface OnFlightClickListener {
        void onFlightClick(Flight flight, double priceDiff);
    }

    public ChangeFlightAdapter(List<Flight> flights, double oldPrice, OnFlightClickListener listener) {
        this.flights = flights;
        this.oldPrice = oldPrice;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_flight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flight flight = flights.get(position);

        holder.tvFlightInfo.setText(flight.getFlightNumber() + " • " + flight.getAircraftModel());
        holder.tvClassName.setText("Phổ thông"); // Assuming
        
        try {
            Date depDate = inputFormat.parse(flight.getDepartureAt());
            Date arrDate = inputFormat.parse(flight.getArrivalAt());
            holder.tvOriginTime.setText(timeFormat.format(depDate));
            holder.tvDestTime.setText(timeFormat.format(arrDate));
        } catch (Exception e) {
            holder.tvOriginTime.setText("--:--");
            holder.tvDestTime.setText("--:--");
        }

        holder.tvOriginCode.setText(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getCode() : "");
        holder.tvDestCode.setText(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getCode() : "");
        
        int duration = flight.getDuration();
        holder.tvDuration.setText((duration / 60) + "h " + (duration % 60) + "m");

        double currentPrice = flight.getBasePrice();
        double diff = currentPrice - oldPrice;
        if (diff > 0) {
            holder.tvPriceDiff.setText("+ " + priceFormat.format(diff) + " VNĐ");
        } else {
            holder.tvPriceDiff.setText("0 VNĐ");
        }

        holder.itemView.setOnClickListener(v -> listener.onFlightClick(flight, Math.max(0, diff)));
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlightInfo, tvClassName, tvPriceDiff, tvOriginCode, tvOriginTime, tvDestCode, tvDestTime, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlightInfo = itemView.findViewById(R.id.tvFlightInfo);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvPriceDiff = itemView.findViewById(R.id.tvPriceDiff);
            tvOriginCode = itemView.findViewById(R.id.tvOriginCode);
            tvOriginTime = itemView.findViewById(R.id.tvOriginTime);
            tvDestCode = itemView.findViewById(R.id.tvDestCode);
            tvDestTime = itemView.findViewById(R.id.tvDestTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
