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
        
        // Nút check-in / quản lý đặt chỗ
        binding.btnCheck.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
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
                    Log.d("FlightsFragment2", "Fetched " + allTickets.size() + " tickets");
                    updateRecyclerView();
                } else {
                    Log.e("FlightsFragment2", "Error: " + response.code());
                    Toast.makeText(requireContext(), "Không thể tải danh sách vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TicketResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("FlightsFragment2", "Failure: " + t.getMessage());
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

        for (TicketResponse res : allTickets) {
            // Lọc theo mẫu mới: "Booked" hoặc "Paid" là vé sắp tới
            String status = res.getStatus() != null ? res.getStatus() : "Booked";
            
            if (isUpcomingTab && "Completed".equalsIgnoreCase(status)) continue;
            if (!isUpcomingTab && !"Completed".equalsIgnoreCase(status)) continue;

            try {
                if (res.getFlight() == null) {
                    Log.e("FlightsFragment2", "Flight data is missing for ticket: " + res.getBookingCode());
                    continue;
                }
                
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

                // Lấy mã số ghế từ seatId (Ví dụ: ..._21D -> 21D)
                String seatNum = res.getSeatId();
                if (seatNum != null && seatNum.contains("_")) {
                    seatNum = seatNum.substring(seatNum.lastIndexOf("_") + 1);
                }

                String displayTicketType = "Một chiều";
                if ("Return".equalsIgnoreCase(res.getTicketType())) {
                    displayTicketType = "Lượt về";
                } else if ("Departure".equalsIgnoreCase(res.getTicketType())) {
                    displayTicketType = "Lượt đi";
                }

                displayTickets.add(new Ticket(
                    day, 
                    monthYear,
                    "Phổ thông",
                    res.getBookingCode(),
                    fromCode, 
                    fromCity, 
                    toCode, 
                    toCity,
                    time, 
                    (seatNum != null ? seatNum : "--"),
                    res.getTotalAmount(),
                    (res.getPassengerName() != null ? res.getPassengerName() : "Hành khách"),
                    displayTicketType
                ));
            } catch (Exception e) {
                Log.e("FlightsFragment2", "Parse error for ticket: " + e.getMessage());
            }
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
        TicketDetailFragment fragment = TicketDetailFragment.newInstance(
            ticket.getFlightNo(), 
            ticket.getOriginCode(), 
            ticket.getOriginCity(),
            ticket.getDestCode(), 
            ticket.getDestCity(),
            ticket.getDay() + " " + ticket.getMonthYear().replace("\n", " "),
            ticket.getTime(), 
            ticket.getSeat(),
            ticket.getPassengerName(),
            ticket.getTotalAmount()
        );
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openCancel(Ticket ticket) {
        CancelTicketFragment fragment = CancelTicketFragment.newInstance(
            ticket.getFlightNo(), 
            ticket.getOriginCode(), 
            ticket.getOriginCity(), 
            ticket.getDestCode(), 
            ticket.getDestCity(), 
            ticket.getDay() + " " + ticket.getMonthYear().replace("\n", " "),
            ticket.getTime(),
            ticket.getTotalAmount()
        );
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    private void openChange(Ticket ticket) {
        getParentFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, new ChangeTicketFragment())
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
