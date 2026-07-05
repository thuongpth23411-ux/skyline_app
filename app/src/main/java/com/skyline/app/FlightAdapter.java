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

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.ViewHolder> {

    private List<Flight> flights;
    private OnFlightClickListener listener;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private DecimalFormat priceFormat = new DecimalFormat("#,###");

    public interface OnFlightClickListener {
        void onFlightClick(Flight flight);
        void onDetailClick(Flight flight);
    }

    public FlightAdapter(List<Flight> flights, OnFlightClickListener listener) {
        this.flights = flights;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flight flight = flights.get(position);

        try {
            Date depDate = inputFormat.parse(flight.getDepartureAt());
            Date arrDate = inputFormat.parse(flight.getArrivalAt());
            holder.tvDepTime.setText(timeFormat.format(depDate));
            holder.tvArrTime.setText(timeFormat.format(arrDate));
        } catch (Exception e) {
            holder.tvDepTime.setText("--:--");
            holder.tvArrTime.setText("--:--");
        }

        holder.tvDepCode.setText(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getCode() : "");
        holder.tvArrCode.setText(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getCode() : "");
        holder.tvAirlineName.setText(flight.getFlightNumber()); // Displaying flight number as primary ID
        holder.tvAircraft.setText(flight.getAircraftModel());
        holder.tvPrice.setText(priceFormat.format(flight.getBasePrice()) + " VND");

        int duration = flight.getDuration();
        int hours = duration / 60;
        int mins = duration % 60;
        holder.tvDuration.setText(hours + "g" + mins + "p");

        holder.btnSelect.setOnClickListener(v -> listener.onFlightClick(flight));
        holder.btnDetail.setOnClickListener(v -> listener.onDetailClick(flight));
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDepTime, tvArrTime, tvDepCode, tvArrCode, tvAirlineName, tvPrice, tvDuration, btnDetail, tvAircraft;
        View btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDepTime = itemView.findViewById(R.id.tvDepTime);
            tvArrTime = itemView.findViewById(R.id.tvArrTime);
            tvDepCode = itemView.findViewById(R.id.tvDepCode);
            tvArrCode = itemView.findViewById(R.id.tvArrCode);
            tvAirlineName = itemView.findViewById(R.id.tvAirlineName);
            tvAircraft = itemView.findViewById(R.id.tvAircraft);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }
}
