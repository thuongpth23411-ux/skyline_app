package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.skyline.app.databinding.FragmentBookBinding;

public class BookFragment extends Fragment {

    private FragmentBookBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTripTypeToggle();
        setupClickListeners();
    }

    private void setupTripTypeToggle() {
        binding.toggleTripType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRoundTrip) {
                    binding.layoutReturnDate.setAlpha(1.0f);
                    binding.tvReturnDate.setText("30/12/2025"); // Example
                } else {
                    binding.layoutReturnDate.setAlpha(0.5f);
                    binding.tvReturnDate.setText("-- / -- / ----");
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSearch.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đang tìm chuyến bay...", Toast.LENGTH_SHORT).show();
        });

        binding.tvFrom.setOnClickListener(v -> Toast.makeText(requireContext(), "Chọn điểm đi", Toast.LENGTH_SHORT).show());
        binding.tvTo.setOnClickListener(v -> Toast.makeText(requireContext(), "Chọn điểm đến", Toast.LENGTH_SHORT).show());
        binding.tvDepDate.setOnClickListener(v -> Toast.makeText(requireContext(), "Chọn ngày đi", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
