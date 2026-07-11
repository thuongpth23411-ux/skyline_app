package com.skyline.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.skyline.app.databinding.FragmentCancelTicketBinding;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            String bookingId = getArguments().getString("bookingId");
            String flightClass = getArguments().getString("flightClass", "Phổ thông");
            String dateStr = getArguments().getString("date");
            
            binding.tvBookingId.setText(getString(R.string.booking_id_label, bookingId));
            binding.tvFromCode.setText(getArguments().getString("fromCode"));
            binding.tvFromCity.setText(getArguments().getString("fromCity"));
            binding.tvToCode.setText(getArguments().getString("toCode"));
            binding.tvToCity.setText(getArguments().getString("toCity"));
            binding.tvDate.setText(dateStr);
            binding.tvTime.setText(getArguments().getString("time"));
            
            double originalPrice = 2450000.0; 
            double cancelFee = (flightClass != null && flightClass.contains("Thương gia")) ? 600000.0 : 300000.0;
            double refundPercent = calculateRefundPercent(dateStr);
            
            // Deduction = originalPrice * (1 - refundPercent)
            double deduction = originalPrice * (1.0 - refundPercent);
            double totalDeduction = deduction + cancelFee;
            double refundAmount = Math.max(0, originalPrice - totalDeduction);

            DecimalFormat df = new DecimalFormat("#,###");
            binding.tvOriginalPrice.setText(df.format(originalPrice) + " VND");
            binding.tvCancelFee.setText("- " + df.format(cancelFee) + " VND");
            
            // Update deduction label and value
            if (binding.tvDeduction != null) {
                binding.tvDeduction.setText("- " + df.format(deduction) + " VND (" + (int)((1-refundPercent)*100) + "%)");
            }
            
            if (refundPercent == 0) {
                binding.tvRefundAmount.setText("KHÔNG HỖ TRỢ");
                binding.btnConfirmCancel.setEnabled(false);
                binding.btnConfirmCancel.setAlpha(0.5f);
            } else {
                binding.tvRefundAmount.setText(df.format(refundAmount) + " VND");
            }
        }
    }

    private double calculateRefundPercent(String dateStr) {
        try {
            // Check current time vs flight date
            // For simplicity, we use the date string. In a real app, parse it to Date object.
            // Mock logic based on the 7-day, 3-day policy
            return 0.7; // 70% refund (30% deduction) as default mock
        } catch (Exception e) {
            return 0.7;
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnBackAction.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnConfirmCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy vé")
                .setMessage("Bạn có chắc chắn muốn hủy vé máy bay này không? Số tiền hoàn lại sẽ được chuyển vào tài khoản của bạn trong 3-5 ngày làm việc.")
                .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Yêu cầu hủy vé đã được gửi thành công.", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                })
                .setNegativeButton("Quay lại", null)
                .show();
        });

        binding.btnPolicy.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new TicketPolicyFragment())
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
