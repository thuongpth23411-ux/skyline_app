package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.skyline.app.databinding.FragmentTicketDetailBinding;

public class TicketDetailFragment extends Fragment {

    private FragmentTicketDetailBinding binding;
    private boolean isUnlocked = false;

    public static TicketDetailFragment newInstance(String flightNo, String origin, String destination, String time, String seat) {
        TicketDetailFragment fragment = new TicketDetailFragment();
        Bundle args = new Bundle();
        args.putString("flightNo", flightNo);
        args.putString("origin", origin);
        args.putString("destination", destination);
        args.putString("time", time);
        args.putString("seat", seat);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTicketDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load data from arguments
        if (getArguments() != null) {
            String flightNo = getArguments().getString("flightNo");
            binding.tvTicketId.setText("SK-2024-" + flightNo);
            binding.tvOriginCode.setText(getArguments().getString("origin"));
            binding.tvDestCode.setText(getArguments().getString("destination"));
            binding.tvTimeRange.setText(getArguments().getString("time") + " - 10:40");
            binding.tvSeatClass.setText(getArguments().getString("seat") + " / Phổ thông");
        }

        setupClickListeners();
        
        // Initial state: blurred effect via alpha
        binding.ivQR.setAlpha(0.1f);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnUnlock.setOnClickListener(v -> {
            if (!isUnlocked) {
                isUnlocked = true;
                binding.ivQR.animate().alpha(1.0f).setDuration(300).start();
                binding.btnUnlock.setVisibility(View.GONE);
            }
        });

        binding.btnCancel.setOnClickListener(v -> {
            if (getArguments() != null) {
                CancelTicketFragment fragment = CancelTicketFragment.newInstance(
                    getArguments().getString("flightNo"),
                    getArguments().getString("origin"),
                    "Hà Nội", // Tạm thời hardcode city hoặc lấy từ Ticket object nếu có
                    getArguments().getString("destination"),
                    "TP. Hồ Chí Minh",
                    "12 Th05, 2024",
                    getArguments().getString("time")
                );
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            }
        });

        binding.btnEdit.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new ChangeTicketFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
