package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.skyline.app.databinding.FragmentChangeTicketBinding;

public class ChangeTicketFragment extends Fragment {

    private FragmentChangeTicketBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangeTicketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        setupMockDates();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnConfirm.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đang xác nhận đổi vé...", Toast.LENGTH_LONG).show();
            // Handle confirmation logic
        });
        
        binding.btnPolicy.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new TicketPolicyFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    private void setupMockDates() {
        // Example logic to highlight T3 - 24
        View dateItem = binding.layoutDates.getChildAt(1);
        if (dateItem != null) {
            View circle = dateItem.findViewById(R.id.tvDayOfMonth);
            if (circle != null) {
                circle.setBackgroundResource(R.drawable.bg_circle_black);
                ((android.widget.TextView)circle).setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
