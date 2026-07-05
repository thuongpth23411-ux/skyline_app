package com.skyline.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.skyline.app.databinding.DialogPriceDetailBinding;
import com.skyline.app.databinding.FragmentFlights2Binding;
import com.skyline.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class FlightsFragment2 extends Fragment {

    private FragmentFlights2Binding binding;
    private boolean isUpcomingTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFlights2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTabs();
        setupRecyclerView();
        updateUI();
    }

    private void setupRecyclerView() {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket(
            "27", "THÁNG 12\n2025",
            getString(R.string.economy_class), "SK9921X",
            "HAN", "Hà Nội",
            "SGN", "TP. Hồ Chí Minh",
            "14:30", "08C"
        ));
        tickets.add(new Ticket(
            "15", "THÁNG 01\n2026",
            getString(R.string.business_class_caps), "SK4402A",
            "DAD", "Đà Nẵng",
            "HUI", "Huế",
            "09:15", "02A"
        ));

        binding.rvTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTickets.setAdapter(new TicketAdapter(tickets, ticket -> showPriceDetailDialog()));
    }

    private void showPriceDetailDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        DialogPriceDetailBinding dialogBinding = DialogPriceDetailBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.btnBack.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupTabs() {
        binding.tabUpcomingContainer.setOnClickListener(v -> {
            if (!isUpcomingTab) {
                isUpcomingTab = true;
                updateUI();
            }
        });

        binding.tabCompletedContainer.setOnClickListener(v -> {
            if (isUpcomingTab) {
                isUpcomingTab = false;
                updateUI();
            }
        });
    }

    private void updateUI() {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary);

        if (isUpcomingTab) {
            binding.tabUpcoming.setTextColor(activeColor);
            binding.tabUpcoming.setTypeface(null, Typeface.BOLD);
            binding.indicatorUpcoming.setVisibility(View.VISIBLE);

            binding.tabCompleted.setTextColor(inactiveColor);
            binding.tabCompleted.setTypeface(null, Typeface.NORMAL);
            binding.indicatorCompleted.setVisibility(View.INVISIBLE);

            binding.rvTickets.setVisibility(View.VISIBLE);
        } else {
            binding.tabCompleted.setTextColor(activeColor);
            binding.tabCompleted.setTypeface(null, Typeface.BOLD);
            binding.indicatorCompleted.setVisibility(View.VISIBLE);

            binding.tabUpcoming.setTextColor(inactiveColor);
            binding.tabUpcoming.setTypeface(null, Typeface.NORMAL);
            binding.indicatorUpcoming.setVisibility(View.INVISIBLE);

            binding.rvTickets.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
