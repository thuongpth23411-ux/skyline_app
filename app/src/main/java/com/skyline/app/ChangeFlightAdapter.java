package com.skyline.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.card.MaterialCardView;
import com.skyline.app.network.Flight;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChangeFlightAdapter extends RecyclerView.Adapter<ChangeFlightAdapter.ViewHolder> {

    private List<Flight> flights;
    private double oldPrice;
    private OnFlightClickListener listener;
    private int selectedPosition = -1;
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
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

        holder.tvFlightInfo.setText(flight.getFlightNumber());
        holder.tvClassName.setText("Phổ thông • " + flight.getAircraftModel());
        
        try {
            Date depDate = inputFormat.parse(flight.getDepartureAt());
            Date arrDate = inputFormat.parse(flight.getArrivalAt());
            if (depDate != null) holder.tvOriginTime.setText(timeFormat.format(depDate));
            else holder.tvOriginTime.setText("--:--");
            
            if (arrDate != null) holder.tvDestTime.setText(timeFormat.format(arrDate));
            else holder.tvDestTime.setText("--:--");
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

        // Highlight selection
        if (selectedPosition == position) {
            holder.cardFlight.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_blue_dark));
            holder.cardFlight.setStrokeWidth(8); // Dày hơn
            holder.cardFlight.setCardElevation(12f); // Nổi bật hơn
            holder.cardFlight.setAlpha(1.0f);
        } else {
            holder.cardFlight.setStrokeColor(Color.parseColor("#F3F4F6"));
            holder.cardFlight.setStrokeWidth(2);
            holder.cardFlight.setCardElevation(2f);
            holder.cardFlight.setAlpha(0.9f); // Hơi mờ các chuyến không chọn
        }

        // Load Airline Logo
        if (flight.getAirline() != null && flight.getAirline().getLogo() != null) {
            String originalUrl = flight.getAirline().getLogo();
            String finalUrl = originalUrl;
            if (originalUrl.toLowerCase().endsWith(".svg") || originalUrl.contains("wikipedia")) {
                finalUrl = "https://images.weserv.nl/?url=" + originalUrl + "&w=200&output=png";
            }
            Glide.with(holder.itemView.getContext())
                    .load(finalUrl)
                    .placeholder(android.R.color.transparent)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.ivAirlineLogo);
        } else {
            holder.ivAirlineLogo.setImageResource(R.drawable.ic_plane);
            holder.ivAirlineLogo.setAlpha(0.3f);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            if (previousSelected != -1) notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onFlightClick(flight, Math.max(0, diff));
        });
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlightInfo, tvClassName, tvPriceDiff, tvOriginCode, tvOriginTime, tvDestCode, tvDestTime, tvDuration;
        ImageView ivAirlineLogo;
        MaterialCardView cardFlight;

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
            ivAirlineLogo = itemView.findViewById(R.id.ivAirlineLogo);
            cardFlight = itemView.findViewById(R.id.cardFlight);
        }
    }
}
