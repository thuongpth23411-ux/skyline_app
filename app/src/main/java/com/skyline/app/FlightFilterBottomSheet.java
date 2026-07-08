package com.skyline.app;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.skyline.app.databinding.BottomSheetFlightFilterBinding;
import com.skyline.app.network.Airline;
import java.util.ArrayList;
import java.util.List;

public class FlightFilterBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetFlightFilterBinding binding;
    private List<Airline> allAirlines;
    private FlightFilter tempFilter;
    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFilterApplied(FlightFilter filter);
    }

    public void setup(List<Airline> allAirlines, FlightFilter currentFilter, OnFilterAppliedListener listener) {
        this.allAirlines = allAirlines;
        this.tempFilter = new FlightFilter();
        this.tempFilter.airlineIds = new ArrayList<>(currentFilter.airlineIds);
        this.tempFilter.priceRangeIndex = currentFilter.priceRangeIndex;
        this.tempFilter.timeSlotIndex = currentFilter.timeSlotIndex;
        this.tempFilter.durationIndex = currentFilter.durationIndex;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetFlightFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (tempFilter == null) {
            dismiss();
            return;
        }

        refreshUI();

        binding.btnReset.setOnClickListener(v -> {
            tempFilter.reset();
            refreshUI();
        });

        binding.btnApply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterApplied(tempFilter);
            }
            dismiss();
        });
    }

    private void refreshUI() {
        setupAirlines();
        setupPriceRanges();
        setupTimeSlots();
        setupDurations();
    }

    private void setupAirlines() {
        if (allAirlines == null) return;
        binding.cgAirlines.removeAllViews();
        for (Airline airline : allAirlines) {
            View itemView = getLayoutInflater().inflate(R.layout.item_filter_chip, binding.cgAirlines, false);
            MaterialCardView card = itemView.findViewById(R.id.cardFilter);
            TextView tv = itemView.findViewById(R.id.tvLabel);
            
            tv.setText(airline.getName());
            boolean isSelected = tempFilter.airlineIds.contains(airline.getName());
            updateItemUI(card, tv, isSelected);

            card.setOnClickListener(v -> {
                if (tempFilter.airlineIds.contains(airline.getName())) {
                    tempFilter.airlineIds.remove(airline.getName());
                } else {
                    tempFilter.airlineIds.add(airline.getName());
                }
                setupAirlines();
            });
            binding.cgAirlines.addView(itemView);
        }
    }

    private void setupPriceRanges() {
        String[] labels = {"< 1.5", "1.5 - 2.5", "2.5 - 4", "≥ 4"};
        binding.cgPrices.removeAllViews();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            View itemView = getLayoutInflater().inflate(R.layout.item_filter_chip, binding.cgPrices, false);
            MaterialCardView card = itemView.findViewById(R.id.cardFilter);
            TextView tv = itemView.findViewById(R.id.tvLabel);
            
            tv.setText(labels[i]);
            updateItemUI(card, tv, tempFilter.priceRangeIndex == index);

            card.setOnClickListener(v -> {
                tempFilter.priceRangeIndex = (tempFilter.priceRangeIndex == index) ? -1 : index;
                setupPriceRanges();
            });
            binding.cgPrices.addView(itemView);
        }
    }

    private void setupTimeSlots() {
        String[] labels = {
            "Nửa đêm\n(00:00-05:59)", 
            "Sáng\n(06:00-11:59)", 
            "Chiều\n(12:00-17:59)", 
            "Tối\n(18:00-23:59)"
        };
        binding.cgTimeSlots.removeAllViews();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            View itemView = getLayoutInflater().inflate(R.layout.item_filter_chip, binding.cgTimeSlots, false);
            MaterialCardView card = itemView.findViewById(R.id.cardFilter);
            TextView tv = itemView.findViewById(R.id.tvLabel);
            
            tv.setText(labels[i]);
            updateItemUI(card, tv, tempFilter.timeSlotIndex == index);

            card.setOnClickListener(v -> {
                tempFilter.timeSlotIndex = (tempFilter.timeSlotIndex == index) ? -1 : index;
                setupTimeSlots();
            });
            binding.cgTimeSlots.addView(itemView);
        }
    }

    private void setupDurations() {
        String[] labels = {"< 60'", "60 - 120'", "> 120'"};
        binding.cgDurations.removeAllViews();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            View itemView = getLayoutInflater().inflate(R.layout.item_filter_chip, binding.cgDurations, false);
            MaterialCardView card = itemView.findViewById(R.id.cardFilter);
            TextView tv = itemView.findViewById(R.id.tvLabel);
            
            tv.setText(labels[i]);
            updateItemUI(card, tv, tempFilter.durationIndex == index);

            card.setOnClickListener(v -> {
                tempFilter.durationIndex = (tempFilter.durationIndex == index) ? -1 : index;
                setupDurations();
            });
            binding.cgDurations.addView(itemView);
        }
    }

    private void updateItemUI(MaterialCardView card, TextView tv, boolean selected) {
        int activeBg = ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark);
        int activeText = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveText = ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary);
        int inactiveStroke = android.graphics.Color.parseColor("#D1D5DB");
        
        card.setCardBackgroundColor(ColorStateList.valueOf(selected ? activeBg : ContextCompat.getColor(requireContext(), R.color.white)));
        card.setStrokeColor(ColorStateList.valueOf(selected ? activeBg : inactiveStroke));
        tv.setTextColor(selected ? activeText : inactiveText);
    }
}
