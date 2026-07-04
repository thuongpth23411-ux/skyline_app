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
import com.skyline.app.databinding.FragmentFlightsBinding;

public class FlightsFragment extends Fragment {
    private FragmentFlightsBinding binding;
    private boolean isUpcomingTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFlightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTabs();
        updateUI();
    }

    private void setupTabs() {
        binding.tabUpcoming.setOnClickListener(v -> {
            isUpcomingTab = true;
            updateUI();
        });

        binding.tabCompleted.setOnClickListener(v -> {
            isUpcomingTab = false;
            updateUI();
        });
    }

    private void updateUI() {
        if (isUpcomingTab) {
            binding.tabUpcoming.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active));
            binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.tabUpcoming.setTypeface(null, Typeface.BOLD);

            binding.tabCompleted.setBackground(null);
            binding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
            binding.tabCompleted.setTypeface(null, Typeface.NORMAL);

            binding.ivEmpty.setImageResource(R.drawable.checkticket_background1);
        } else {
            binding.tabCompleted.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_active));
            binding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.tabCompleted.setTypeface(null, Typeface.BOLD);

            binding.tabUpcoming.setBackground(null);
            binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
            binding.tabUpcoming.setTypeface(null, Typeface.NORMAL);

            binding.ivEmpty.setImageResource(R.drawable.checkticket_background2);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
