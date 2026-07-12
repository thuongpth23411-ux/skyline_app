package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemChangeFlightBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.RetrofitClient;
import com.bumptech.glide.Glide;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChangeFlightAdapter extends RecyclerView.Adapter<ChangeFlightAdapter.ViewHolder> {

    private final List<Flight> flights;
    private final OnFlightSelectedListener listener;
    private final double oldBasePrice;
    private final String targetClass;
    private int selectedPos = -1;
    private final SimpleDateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnFlightSelectedListener {
        void onFlightSelected(Flight flight);
    }

    public ChangeFlightAdapter(List<Flight> flights, double oldBasePrice, String targetClass, OnFlightSelectedListener listener) {
        this.flights = flights;
        this.oldBasePrice = oldBasePrice;
        this.targetClass = targetClass;
        this.listener = listener;
        isoParser.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChangeFlightBinding binding = ItemChangeFlightBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flight f = flights.get(position);
        holder.binding.tvFlightInfo.setText(String.format("%s • %s", f.getFlightNumber(), f.getAircraftModel()));
        
        String displayClass = "Phổ thông";
        double newPrice = f.getBasePrice();
        
        if (targetClass != null && targetClass.contains("Thương gia")) {
            displayClass = "Thương gia";
            // Tìm giá Thương gia trong priceOptions
            if (f.getPriceOptions() != null) {
                for (Flight.PriceOption opt : f.getPriceOptions()) {
                    if ("BUSINESS".equalsIgnoreCase(opt.getType())) {
                        newPrice = opt.getPrice();
                        break;
                    }
                }
            }
        }
        
        holder.binding.tvClassName.setText(displayClass); 
        
        DecimalFormat df = new DecimalFormat("#,###");
        double diff = Math.max(0, newPrice - oldBasePrice);
        holder.binding.tvPriceDiff.setText(String.format("+ %s VNĐ", df.format(diff)));

        holder.binding.tvOriginCode.setText(f.getDepartureAirport() != null ? f.getDepartureAirport().getCode() : "---");
        holder.binding.tvDestCode.setText(f.getArrivalAirport() != null ? f.getArrivalAirport().getCode() : "---");

        if (f.getAirline() != null && f.getAirline().getLogo() != null) {
            Glide.with(holder.itemView.getContext())
                .load(RetrofitClient.formatUrl(f.getAirline().getLogo()))
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.binding.ivAirlineLogo);
        } else {
            holder.binding.ivAirlineLogo.setImageResource(R.drawable.logo);
        }

        try {
            Date dep = isoParser.parse(f.getDepartureAt());
            Date arr = isoParser.parse(f.getArrivalAt());
            if (dep != null) holder.binding.tvOriginTime.setText(timeFormat.format(dep));
            if (arr != null) holder.binding.tvDestTime.setText(timeFormat.format(arr));
        } catch (Exception ignored) {}

        // Selection UI - Blue border only
        boolean isSelected = selectedPos == position;
        int strokeColor = isSelected ? ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_blue) 
                                   : ContextCompat.getColor(holder.itemView.getContext(), R.color.skyline_bg);

        holder.binding.getRoot().setStrokeColor(strokeColor);
        holder.binding.getRoot().setStrokeWidth(isSelected ? 6 : 2);
        holder.binding.getRoot().setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));

        holder.itemView.setOnClickListener(v -> {
            int old = selectedPos;
            selectedPos = holder.getBindingAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selectedPos);
            if (listener != null) listener.onFlightSelected(f);
        });
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemChangeFlightBinding binding;
        ViewHolder(ItemChangeFlightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
