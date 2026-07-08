package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skyline.app.databinding.BottomSheetFlightDetailBinding;
import com.skyline.app.network.Flight;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FlightDetailBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetFlightDetailBinding binding;
    private Flight flight;
    private OnFlightSelectedListener listener;

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy", Locale.getDefault());

    public interface OnFlightSelectedListener {
        void onFlightSelected(Flight flight);
    }

    public static FlightDetailBottomSheet newInstance(Flight flight, OnFlightSelectedListener listener) {
        FlightDetailBottomSheet fragment = new FlightDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("flight", flight);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetFlightDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            flight = (Flight) getArguments().getSerializable("flight");
        }

        if (flight == null) {
            dismiss();
            return;
        }

        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        setupData();

        binding.btnSelectFlight.setOnClickListener(v -> {
            if (listener != null) listener.onFlightSelected(flight);
            dismiss();
        });
    }

    private void setupData() {
        if (flight.getAirline() != null && flight.getAirline().getLogo() != null) {
            String logoUrl = flight.getAirline().getLogo();
            if (logoUrl.toLowerCase().endsWith(".svg") || logoUrl.contains("wikipedia")) {
                logoUrl = "https://images.weserv.nl/?url=" + logoUrl + "&w=400&output=png";
            }
            Glide.with(this)
                    .load(logoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivAirlineLogo);
        }

        binding.tvFlightNumber.setText(flight.getFlightNumber());
        binding.tvAircraft.setText(flight.getAircraftModel());

        if (flight.getDepartureAirport() != null) {
            binding.tvDepCode.setText(flight.getDepartureAirport().getCode());
            binding.tvDepCity.setText(flight.getDepartureAirport().getCity());
            binding.tvDepAirportName.setText(flight.getDepartureAirport().getName());
        }
        if (flight.getArrivalAirport() != null) {
            binding.tvArrCode.setText(flight.getArrivalAirport().getCode());
            binding.tvArrCity.setText(flight.getArrivalAirport().getCity());
            binding.tvArrAirportName.setText(flight.getArrivalAirport().getName());
        }

        try {
            Date depDate = inputFormat.parse(flight.getDepartureAt());
            Date arrDate = inputFormat.parse(flight.getArrivalAt());
            if (depDate != null) {
                binding.tvDepTime.setText(timeFormat.format(depDate));
                binding.tvDepDate.setText(dateFormat.format(depDate));
            }
            if (arrDate != null) {
                binding.tvArrTime.setText(timeFormat.format(arrDate));
                binding.tvArrDate.setText(dateFormat.format(arrDate));
            }
        } catch (Exception e) {
            binding.tvDepTime.setText("--:--");
            binding.tvArrTime.setText("--:--");
        }

        binding.tvStops.setText("0");
        binding.tvTotalDuration.setText(String.format(Locale.getDefault(), "%d'", flight.getDuration()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
