package com.skyline.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.FragmentChangeTicketBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSearchRequest;
import com.skyline.app.network.RetrofitClient;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeTicketFragment extends Fragment {

    private FragmentChangeTicketBinding binding;
    private double oldPrice = 0;
    private String fromCode, toCode, selectedDateStr;
    private DecimalFormat priceFormat = new DecimalFormat("#,###");
    private final List<DateSelectorAdapter.DateItem> dateItems = new ArrayList<>();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static ChangeTicketFragment newInstance(String bookingId, String fromCode, String toCode, double oldPrice) {
        ChangeTicketFragment fragment = new ChangeTicketFragment();
        Bundle args = new Bundle();
        args.putString("bookingId", bookingId);
        args.putString("fromCode", fromCode);
        args.putString("toCode", toCode);
        args.putDouble("oldPrice", oldPrice);
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
            fromCode = getArguments().getString("fromCode");
            toCode = getArguments().getString("toCode");
            oldPrice = getArguments().getDouble("oldPrice", 0);
        }

        setupClickListeners();
        setupDateSelector();
        
        // Mặc định chọn ngày đầu tiên trong danh sách (ngày mai)
        if (!dateItems.isEmpty()) {
            selectedDateStr = apiDateFormat.format(dateItems.get(0).date);
            fetchAvailableFlights(selectedDateStr);
        } else {
            selectedDateStr = apiDateFormat.format(new Date());
            fetchAvailableFlights(selectedDateStr);
        }
    }

    private void setupDateSelector() {
        dateItems.clear();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // Bắt đầu từ ngày mai (chỉ các ngày chưa tới mới đổi được)
        cal.add(Calendar.DAY_OF_MONTH, 1);

        for (int i = 0; i < 14; i++) {
            dateItems.add(new DateSelectorAdapter.DateItem(cal.getTime(), -1, i == 0));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        DateSelectorAdapter adapter = new DateSelectorAdapter(dateItems, (date, position) -> {
            selectedDateStr = apiDateFormat.format(date);
            fetchAvailableFlights(selectedDateStr);
        });

        binding.rvDateSelector.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvDateSelector.setAdapter(adapter);
    }

    private void fetchAvailableFlights(String date) {
        if (fromCode == null || toCode == null) return;

        FlightSearchRequest request = new FlightSearchRequest(fromCode, toCode, date);
        RetrofitClient.getInstance().searchFlights(request).enqueue(new Callback<List<Flight>>() {
            @Override
            public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    setupRecyclerView(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Flight>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi tải chuyến bay: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupRecyclerView(List<Flight> flights) {
        ChangeFlightAdapter adapter = new ChangeFlightAdapter(flights, oldPrice, (flight, priceDiff) -> {
            updateSummary(priceDiff);
        });
        binding.rvFlights.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFlights.setAdapter(adapter);
    }

    private void updateSummary(double priceDiff) {
        double changeFee = 600000;
        double total = changeFee + priceDiff;

        binding.tvChangeFee.setText(priceFormat.format(changeFee) + " VNĐ");
        binding.tvPriceDiffTotal.setText("+ " + priceFormat.format(priceDiff) + " VNĐ");
        binding.tvTotalCost.setText(priceFormat.format(total) + " VNĐ");
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnConfirm.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đang xác nhận đổi vé...", Toast.LENGTH_LONG).show();
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
