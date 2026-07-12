package com.skyline.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.skyline.app.databinding.FragmentTicketDetailBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.model.Ticket;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketDetailFragment extends Fragment {

    private FragmentTicketDetailBinding binding;
    private boolean isUnlocked = false;
    private Ticket ticketData;

    public static TicketDetailFragment newInstance(Ticket ticket) {
        TicketDetailFragment fragment = new TicketDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("ticket_data", (Serializable) ticket);
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

        if (getArguments() != null) {
            ticketData = (Ticket) getArguments().getSerializable("ticket_data");
            if (ticketData != null) {
                loadTicketData(ticketData);
            }
        }

        setupClickListeners();
        
        binding.ivQR.setAlpha(0.05f);
        binding.btnUnlock.setVisibility(View.VISIBLE);
    }

    private void loadTicketData(Ticket ticket) {
        binding.tvTicketId.setText(ticket.getFlightNo());
        binding.tvOriginCode.setText(ticket.getOriginCode());
        binding.tvOriginCity.setText(ticket.getOriginCity());
        binding.tvDestCode.setText(ticket.getDestCode());
        binding.tvDestCity.setText(ticket.getDestCity());
        binding.tvDate.setText(ticket.getDay() + " " + ticket.getMonthYear().replace("\n", " "));
        binding.tvTimeRange.setText(ticket.getTime());
        binding.tvPassenger.setText(ticket.getPassengerName());
        binding.tvSeatClass.setText(ticket.getSeat() + " / " + ticket.getFlightClass());
        binding.tvBaggage.setText(ticket.getBaggage() != null ? ticket.getBaggage() : "Không có hành lý mua thêm");

        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=SKYLINE_" + ticket.getFlightNo();
        Glide.with(this).load(qrUrl).placeholder(R.drawable.bg_square_placeholder).into(binding.ivQR);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnUnlock.setOnClickListener(v -> {
            if (!isUnlocked) {
                isUnlocked = true;
                binding.ivQR.animate().alpha(1.0f).setDuration(300).start();
                binding.btnUnlock.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Mã QR đã được mở khóa", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            showCustomConfirmDialog("Lưu vé điện tử", 
                "Quý khách có muốn lưu hình ảnh vé này vào thư viện ảnh để dễ dàng sử dụng khi làm thủ tục không?",
                "Để sau", "Lưu ngay", () -> saveTicketToGallery());
        });

        binding.btnShare.setOnClickListener(v -> {
            com.skyline.app.utils.SessionManager sm = new com.skyline.app.utils.SessionManager(requireContext());
            String targetEmail = sm.getUserEmail();
            showCustomConfirmDialog("Gửi vé qua Email",
                "Skyline sẽ gửi một bản sao vé điện tử đến email " + targetEmail + " của quý khách. Bạn có muốn thực hiện không?",
                "Hủy", "Gửi ngay", () -> shareTicketViaEmail(targetEmail));
        });

        binding.btnCancel.setOnClickListener(v -> {
            if (ticketData != null) {
                CancelTicketFragment fragment = CancelTicketFragment.newInstance(
                    ticketData.getFlightNo(),
                    ticketData.getOriginCode(),
                    ticketData.getOriginCity(),
                    ticketData.getDestCode(),
                    ticketData.getDestCity(),
                    ticketData.getDay() + " " + ticketData.getMonthYear().replace("\n", " "),
                    ticketData.getTime()
                );
                Bundle args = fragment.getArguments();
                if (args != null) {
                    args.putString("flightClass", ticketData.getFlightClass());
                }
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            }
        });

        binding.btnEdit.setOnClickListener(v -> {
            if (ticketData != null) {
                ChangeTicketFragment fragment = ChangeTicketFragment.newInstance(ticketData);
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            }
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

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
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

    private void shareTicketViaEmail(String email) {
        if (ticketData == null) return;
        
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("bookingCode", ticketData.getFlightNo());
        
        RetrofitClient.getInstance().shareTicket(body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Vé đã được gửi thành công về email: " + email, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi gửi email. Thử lại sau.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối máy chủ.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTicketToGallery() {
        binding.getRoot().postDelayed(() -> {
            if (isAdded()) {
                new AlertDialog.Builder(requireContext())
                    .setIcon(R.drawable.ic_check_auth)
                    .setTitle("Thành công")
                    .setMessage("Đã lưu vé điện tử thành công vào thư mục Pictures.")
                    .setPositiveButton("Đóng", null)
                    .show();
            }
        }, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
