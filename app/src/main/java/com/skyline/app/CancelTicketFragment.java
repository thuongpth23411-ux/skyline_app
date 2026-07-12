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
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            String bookingId = getArguments() != null ? getArguments().getString("bookingId") : null;
            if (bookingId == null) return;

            showCustomConfirmDialog("Xác nhận hủy vé",
                "Bạn có chắc chắn muốn hủy vé máy bay này không? Số tiền hoàn lại sẽ được chuyển vào tài khoản của bạn trong 3-5 ngày làm việc.",
                "Quay lại", "Xác nhận hủy", () -> performCancel(bookingId));
        });

        binding.btnPolicy.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new TicketPolicyFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    private void showCustomConfirmDialog(String title, String message, String negBtn, String posBtn, Runnable onPositive) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom_confirm, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        android.widget.TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
        com.google.android.material.button.MaterialButton btnNeg = dialogView.findViewById(R.id.btnNegative);
        com.google.android.material.button.MaterialButton btnPos = dialogView.findViewById(R.id.btnPositive);

        tvTitle.setText(title);
        tvMsg.setText(message);
        btnNeg.setText(negBtn);
        btnPos.setText(posBtn);

        btnNeg.setOnClickListener(v -> dialog.dismiss());
        btnPos.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });

        dialog.show();
    }

    private void performCancel(String bookingCode) {
        String token = "Bearer " + new com.skyline.app.utils.SessionManager(requireContext()).fetchAuthToken();
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("bookingCode", bookingCode);

        com.skyline.app.network.RetrofitClient.getInstance().cancelTicket(token, body).enqueue(new retrofit2.Callback<com.skyline.app.network.BaseResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.skyline.app.network.BaseResponse> call, retrofit2.Response<com.skyline.app.network.BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Đã hủy vé thành công.", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Không thể hủy vé. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.skyline.app.network.BaseResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
