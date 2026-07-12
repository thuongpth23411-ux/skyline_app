package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import com.google.gson.Gson;
import com.skyline.app.databinding.FragmentChangeTicketBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSearchRequest;
import com.skyline.app.network.RetrofitClient;
import com.skyline.model.Ticket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeTicketFragment extends Fragment {

    private FragmentChangeTicketBinding binding;
    private ChangeFlightAdapter flightAdapter;
    private DateSelectorAdapter dateAdapter;
    private final Gson gson = new Gson();
    private Ticket oldTicket;
    private String selectedDateStr;
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final List<Flight> allFlightsForDay = new ArrayList<>();
    private final List<Flight> filteredFlights = new ArrayList<>();
    private final List<DateSelectorAdapter.DateItem> dateItems = new ArrayList<>();
    private Flight selectedFlight;
    private int sortMode = 0;
    private double currentTotal = 0;
    private double currentFee = 0;
    private double currentDiff = 0;
    private double oldBasePrice = 0;

    public static ChangeTicketFragment newInstance(Ticket ticket) {
        ChangeTicketFragment fragment = new ChangeTicketFragment();
        Bundle args = new Bundle();
        args.putSerializable("old_ticket", ticket);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangeTicketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            oldTicket = (Ticket) getArguments().getSerializable("old_ticket");
        }

        if (oldTicket == null) return;

        // Ước tính giá gốc (trừ thuế phí 450k + 10% VAT)
        oldBasePrice = (oldTicket.getTotalAmount() - 450000) / 1.1;

        binding.tvFromCity.setText(oldTicket.getOriginCity());
        binding.tvToCity.setText(oldTicket.getDestCity());

        setupClickListeners();
        setupDateSelector();
        setupRecyclerView();
        
        searchFlights(selectedDateStr);
    }

    private void setupDateSelector() {
        dateItems.clear();
        Calendar cal = Calendar.getInstance();
        
        try {
            if (oldTicket.getFullDate() != null) {
                Date d = apiDateFormat.parse(oldTicket.getFullDate());
                if (d != null) cal.setTime(d);
            }
        } catch (Exception ignored) {}
        
        selectedDateStr = apiDateFormat.format(cal.getTime());

        for (int i = 0; i < 8; i++) {
            Date d = cal.getTime();
            boolean isSelected = i == 0;
            dateItems.add(new DateSelectorAdapter.DateItem(d, 0, isSelected));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        dateAdapter = new DateSelectorAdapter(dateItems, (date, pos) -> {
            selectedDateStr = apiDateFormat.format(date);
            dateAdapter.updateSelection(pos);
            searchFlights(selectedDateStr);
        });
        
        binding.rvDateSelector.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvDateSelector.setAdapter(dateAdapter);
        new LinearSnapHelper().attachToRecyclerView(binding.rvDateSelector);
    }

    private void setupRecyclerView() {
        binding.rvFlights.setLayoutManager(new LinearLayoutManager(requireContext()));
        flightAdapter = new ChangeFlightAdapter(filteredFlights, oldBasePrice, oldTicket.getFlightClass(), flight -> {
            selectedFlight = flight;
            updateSummary();
        });
        binding.rvFlights.setAdapter(flightAdapter);
    }

    private void searchFlights(String date) {
        if (binding == null || oldTicket == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance().searchFlights(new FlightSearchRequest(oldTicket.getOriginCode(), oldTicket.getDestCode(), date))
            .enqueue(new Callback<List<Flight>>() {
                @Override
                public void onResponse(@NonNull Call<List<Flight>> call, @NonNull Response<List<Flight>> response) {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        allFlightsForDay.clear();
                        allFlightsForDay.addAll(response.body());
                        applySort();
                    }
                }
                @Override public void onFailure(@NonNull Call<List<Flight>> call, @NonNull Throwable t) {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Lỗi tải chuyến bay", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void applySort() {
        filteredFlights.clear();
        filteredFlights.addAll(allFlightsForDay);
        if (sortMode == 1) Collections.sort(filteredFlights, (f1, f2) -> Double.compare(f2.getBasePrice(), f1.getBasePrice()));
        else if (sortMode == 2) Collections.sort(filteredFlights, (f1, f2) -> Double.compare(f1.getBasePrice(), f2.getBasePrice()));
        flightAdapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        if (selectedFlight == null || oldTicket == null) return;

        double newPrice = selectedFlight.getBasePrice();
        String tClass = oldTicket.getFlightClass();
        if (tClass != null && tClass.contains("Thương gia")) {
            if (selectedFlight.getPriceOptions() != null) {
                for (Flight.PriceOption opt : selectedFlight.getPriceOptions()) {
                    if ("BUSINESS".equalsIgnoreCase(opt.getType())) {
                        newPrice = opt.getPrice();
                        break;
                    }
                }
            }
        }
        
        currentDiff = Math.max(0, newPrice - oldBasePrice);
        
        currentFee = (tClass != null && tClass.contains("Thương gia")) ? 600000.0 : 300000.0;
        currentTotal = currentDiff + currentFee;

        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvChangeFee.setText(df.format(currentFee) + " VNĐ");
        binding.tvPriceDiff.setText("+ " + df.format(currentDiff) + " VNĐ");
        binding.tvTotalCost.setText(df.format(currentTotal) + " VNĐ");
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        binding.btnSortDesc.setOnClickListener(v -> { sortMode = 1; applySort(); updateSortUI(); });
        binding.btnSortAsc.setOnClickListener(v -> { sortMode = 2; applySort(); updateSortUI(); });
        
        binding.btnConfirm.setOnClickListener(v -> {
            if (selectedFlight == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn chuyến bay muốn đổi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gán ghế ngẫu nhiên: ví dụ 12B, 15A
            String randomSeat = (10 + (int)(Math.random() * 20)) + "" + (char)('A' + (int)(Math.random() * 6));

            Intent intent = new Intent(requireContext(), ConfirmPaymentActivity.class);
            intent.putExtra("totalAmount", currentTotal);
            intent.putExtra("passenger_name", oldTicket.getPassengerName());
            intent.putExtra("passenger_email", new com.skyline.app.utils.SessionManager(requireContext()).getUserEmail());
            intent.putExtra("selected_seat", randomSeat);
            intent.putExtra("flight_json", gson.toJson(selectedFlight));
            intent.putExtra("is_exchange", true);
            intent.putExtra("exchange_fee", currentFee);
            intent.putExtra("price_diff", currentDiff);
            intent.putExtra("fare_type", oldTicket.getFlightClass());
            intent.putExtra("old_ticket_id", oldTicket.getFlightNo()); // bookingCode dùng làm định danh tạm
            startActivity(intent);
        });
        
        binding.btnPolicy.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new TicketPolicyFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    private void updateSortUI() {
        int active = ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark);
        int normal = ContextCompat.getColor(requireContext(), R.color.white);
        binding.btnSortDesc.setBackgroundColor(sortMode == 1 ? active : normal);
        binding.btnSortDesc.setTextColor(sortMode == 1 ? normal : active);
        binding.btnSortAsc.setBackgroundColor(sortMode == 2 ? active : normal);
        binding.btnSortAsc.setTextColor(sortMode == 2 ? normal : active);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
