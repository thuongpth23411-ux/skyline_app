package com.skyline.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.FragmentFlights2Binding;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.TicketResponse;
import com.skyline.app.utils.SessionManager;
import com.skyline.model.Ticket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightsFragment2 extends Fragment {

    private FragmentFlights2Binding binding;
    private boolean isUpcomingTab = true;
    private SessionManager sessionManager;
    private List<TicketResponse> allTickets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFlights2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        setupTabs();
        setupClickListeners();
        fetchTickets();
        updateUI();
    }

    private void setupClickListeners() {
        binding.btnBookNow.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                getActivity().findViewById(R.id.navBook).performClick();
            }
        });
        
        binding.btnCheck.setOnClickListener(v -> showCheckBookingDialog());
    }

    private void showCheckBookingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_check_booking, null);
        com.google.android.material.textfield.TextInputEditText etBookingCode = dialogView.findViewById(R.id.etBookingCode);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Tìm kiếm", null) // Set null to override later
            .setNegativeButton("Hủy", null)
            .create();

        // Bo góc cho Dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card_white);
        }

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String code = etBookingCode.getText().toString().trim();
                if (!code.isEmpty()) {
                    searchAndOpenTicket(code, dialog);
                } else {
                    Toast.makeText(requireContext(), "Vui lòng nhập mã vé", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void searchAndOpenTicket(String code, androidx.appcompat.app.AlertDialog dialog) {
        TicketResponse target = null;
        for (TicketResponse res : allTickets) {
            if (code.equalsIgnoreCase(res.getBookingCode())) {
                target = res;
                break;
            }
        }

        if (target != null) {
            if (dialog != null) dialog.dismiss();
            // Convert to model Ticket and open
            try {
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat monthYearFormat = new SimpleDateFormat("'THÁNG' MM\nyyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
                
                Date depDate = inputFormat.parse(target.getFlight().getDepartureAt());
                String seatNum = target.getSeatId();
                if (seatNum != null && seatNum.contains("_")) {
                    seatNum = seatNum.substring(seatNum.lastIndexOf("_") + 1);
                }

                Ticket ticket = new Ticket(
                    dayFormat.format(depDate), 
                    monthYearFormat.format(depDate).toUpperCase(),
                    "Phổ thông",
                    target.getBookingCode(),
                    target.getFlight().getDepartureAirport().getCode(),
                    target.getFlight().getDepartureAirport().getCity(),
                    target.getFlight().getArrivalAirport().getCode(),
                    target.getFlight().getArrivalAirport().getCity(),
                    timeFormat.format(depDate),
                    seatNum != null ? seatNum : "--",
                    target.getTotalAmount(),
                    target.getPassengerName(),
                    target.getTicketType()
                );
                if (target.getFlight() != null && target.getFlight().getAirline() != null) {
                    ticket.setAirlineLogoUrl(RetrofitClient.formatUrl(target.getFlight().getAirline().getLogo()));
                }
                ticket.setBaggage(target.getBaggage() != null ? target.getBaggage() : "Đã bao gồm 7kg xách tay");
                openDetail(ticket);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Lỗi khi xử lý dữ liệu vé", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Không tìm thấy, hiện thông báo
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Thông báo")
                .setMessage("Không tìm thấy thông tin vé với mã: " + code + ". Vui lòng kiểm tra lại.")
                .setPositiveButton("Đóng", null)
                .show();
        }
    }

    private void fetchTickets() {
        if (!sessionManager.isLoggedIn()) {
            binding.rvTickets.setVisibility(View.GONE);
            binding.layoutNoFlights.setVisibility(View.VISIBLE);
            return;
        }

        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getMyTickets(token).enqueue(new Callback<List<TicketResponse>>() {
            @Override
            public void onResponse(Call<List<TicketResponse>> call, Response<List<TicketResponse>> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    allTickets = response.body();
                    updateRecyclerView();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TicketResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView() {
        List<Ticket> displayTickets = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("'THÁNG' MM\nyyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (TicketResponse res : allTickets) {
            String status = res.getStatus() != null ? res.getStatus() : "Booked";
            
            // Log logic status based on current date
            boolean isPast = false;
            try {
                if (res.getFlight() != null && res.getFlight().getDepartureAt() != null) {
                    Date depDate = inputFormat.parse(res.getFlight().getDepartureAt());
                    if (depDate != null && depDate.before(new Date())) {
                        isPast = true;
                    }
                }
            } catch (Exception ignored) {}

            String effectiveStatus = status;
            if ("Booked".equalsIgnoreCase(status) && isPast) {
                effectiveStatus = "Completed";
            }

            if (isUpcomingTab) {
                // Tab Sắp tới: Chỉ hiện "Booked" mà chưa quá ngày
                if (!"Booked".equalsIgnoreCase(effectiveStatus)) continue;
            } else {
                // Tab Hoàn thành: Hiện Completed, Cancelled, Disabled
                if ("Booked".equalsIgnoreCase(effectiveStatus)) continue;
            }

            try {
                if (res.getFlight() == null) continue;
                
                Date depDate = null;
                if (res.getFlight().getDepartureAt() != null) {
                    depDate = inputFormat.parse(res.getFlight().getDepartureAt());
                }
                
                if (depDate == null) depDate = new Date(); 

                String day = dayFormat.format(depDate);
                String monthYear = monthYearFormat.format(depDate).toUpperCase();
                String time = timeFormat.format(depDate);

                String fromCode = (res.getFlight().getDepartureAirport() != null) ? res.getFlight().getDepartureAirport().getCode() : "???";
                String fromCity = (res.getFlight().getDepartureAirport() != null) ? res.getFlight().getDepartureAirport().getCity() : "Sân bay đi";
                String toCode = (res.getFlight().getArrivalAirport() != null) ? res.getFlight().getArrivalAirport().getCode() : "???";
                String toCity = (res.getFlight().getArrivalAirport() != null) ? res.getFlight().getArrivalAirport().getCity() : "Sân bay đến";

                String seatNum = res.getSeatId();
                if (seatNum != null && seatNum.contains("_")) {
                    seatNum = seatNum.substring(seatNum.lastIndexOf("_") + 1);
                }

                String displayTicketType = "Một chiều";
                if ("Return".equalsIgnoreCase(res.getTicketType()) || "Khứ hồi - Về".equalsIgnoreCase(res.getTicketType())) {
                    displayTicketType = "Khứ hồi - Về";
                } else if ("Departure".equalsIgnoreCase(res.getTicketType()) || "Khứ hồi - Đi".equalsIgnoreCase(res.getTicketType())) {
                    displayTicketType = "Khứ hồi - Đi";
                }

                Ticket ticket = new Ticket(
                    day, monthYear,
                    "Phổ thông",
                    res.getBookingCode(),
                    fromCode, fromCity, toCode, toCity,
                    time, seatNum != null ? seatNum : "--",
                    res.getTotalAmount(),
                    res.getPassengerName() != null ? res.getPassengerName() : "Hành khách",
                    displayTicketType
                );
                if (res.getFlight() != null && res.getFlight().getAirline() != null) {
                    ticket.setAirlineLogoUrl(RetrofitClient.formatUrl(res.getFlight().getAirline().getLogo()));
                }
                ticket.setFullDate(apiDateFormat.format(depDate));
                ticket.setStatus(effectiveStatus);
                ticket.setBaggage(res.getBaggage() != null ? res.getBaggage() : "Đã bao gồm 7kg xách tay");
                displayTickets.add(ticket);
            } catch (Exception ignored) {}
        }

        if (displayTickets.isEmpty()) {
            binding.rvTickets.setVisibility(View.GONE);
            binding.layoutNoFlights.setVisibility(View.VISIBLE);
        } else {
            binding.rvTickets.setVisibility(View.VISIBLE);
            binding.layoutNoFlights.setVisibility(View.GONE);
            
            binding.rvTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvTickets.setAdapter(new TicketAdapter(displayTickets, new TicketAdapter.OnTicketActionListener() {
                @Override
                public void onDetailClick(Ticket ticket) {
                    openDetail(ticket);
                }

                @Override
                public void onCancelClick(Ticket ticket) {
                    openCancel(ticket);
                }

                @Override
                public void onChangeClick(Ticket ticket) {
                    openChange(ticket);
                }
            }));
        }
    }

    private void openDetail(Ticket ticket) {
        TicketDetailFragment fragment = TicketDetailFragment.newInstance(ticket);
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openCancel(Ticket ticket) {
        CancelTicketFragment fragment = CancelTicketFragment.newInstance(
            ticket.getFlightNo(), ticket.getOriginCode(), ticket.getOriginCity(), 
            ticket.getDestCode(), ticket.getDestCity(), 
            ticket.getDay() + " " + ticket.getMonthYear().replace("\n", " "),
            ticket.getTime()
        );
        // Truyền thêm class để tính phí
        Bundle args = fragment.getArguments();
        if (args != null) {
            args.putString("flightClass", ticket.getFlightClass());
        }
        
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openChange(Ticket ticket) {
        ChangeTicketFragment fragment = ChangeTicketFragment.newInstance(ticket);
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void setupTabs() {
        binding.tabUpcomingContainer.setOnClickListener(v -> {
            if (!isUpcomingTab) {
                isUpcomingTab = true;
                updateUI();
                updateRecyclerView();
            }
        });

        binding.tabCompletedContainer.setOnClickListener(v -> {
            if (isUpcomingTab) {
                isUpcomingTab = false;
                updateUI();
                updateRecyclerView();
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
        } else {
            binding.tabCompleted.setTextColor(activeColor);
            binding.tabCompleted.setTypeface(null, Typeface.BOLD);
            binding.indicatorCompleted.setVisibility(View.VISIBLE);
            binding.tabUpcoming.setTextColor(inactiveColor);
            binding.tabUpcoming.setTypeface(null, Typeface.NORMAL);
            binding.indicatorUpcoming.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
