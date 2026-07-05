package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.skyline.app.databinding.FragmentCancelTicketBinding;

public class CancelTicketFragment extends Fragment {

    private FragmentCancelTicketBinding binding;

    public static CancelTicketFragment newInstance(String bookingId, String fromCode, String fromCity, String toCode, String toCity, String date, String time) {
        CancelTicketFragment fragment = new CancelTicketFragment();
        Bundle args = new Bundle();
        args.putString("bookingId", bookingId);
        args.putString("fromCode", fromCode);
        args.putString("fromCity", fromCity);
        args.putString("toCode", toCode);
        args.putString("toCity", toCity);
        args.putString("date", date);
        args.putString("time", time);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCancelTicketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadTicketData();
        setupClickListeners();
    }

    private void loadTicketData() {
        if (getArguments() != null) {
            binding.tvBookingId.setText(getString(R.string.booking_id_label, getArguments().getString("bookingId")));
            binding.tvFromCode.setText(getArguments().getString("fromCode"));
            binding.tvFromCity.setText(getArguments().getString("fromCity"));
            binding.tvToCode.setText(getArguments().getString("toCode"));
            binding.tvToCity.setText(getArguments().getString("toCity"));
            binding.tvDate.setText(getArguments().getString("date"));
            binding.tvTime.setText(getArguments().getString("time"));
            
            // Giả lập dữ liệu hoàn tiền (có thể load từ API sau này)
            binding.tvOriginalPrice.setText("2,450,000 VND");
            binding.tvCancelFee.setText("- 600,000 VND");
            binding.tvRefundAmount.setText("1,850,000 VND");
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnBackAction.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnConfirmCancel.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Yêu cầu hủy vé đang được xử lý. Tiền sẽ được hoàn về thẻ của bạn.", Toast.LENGTH_LONG).show();
            // Quay về màn hình chính
            getParentFragmentManager().popBackStack();
        });

        binding.btnPolicy.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đang tải chính sách hoàn vé...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
