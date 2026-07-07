package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityFlightResultsBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSearchRequest;
import com.skyline.app.network.RetrofitClient;
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

public class FlightResultsActivity extends AppCompatActivity {

    private ActivityFlightResultsBinding binding;
    private FlightAdapter flightAdapter;
    private DateSelectorAdapter dateAdapter;
    private final Gson gson = new Gson();
    private String fromCode, toCode, selectedDateStr;
    private final List<DateSelectorAdapter.DateItem> dateItems = new ArrayList<>();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlightResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        fromCode = getIntent().getStringExtra("fromCode");
        toCode = getIntent().getStringExtra("toCode");
        selectedDateStr = getIntent().getStringExtra("date");
        String fromCity = getIntent().getStringExtra("fromCity");
        String toCity = getIntent().getStringExtra("toCity");

        binding.tvFromCity.setText(fromCity != null ? fromCity : fromCode);
        binding.tvToCity.setText(toCity != null ? toCity : toCode);
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnClose.setOnClickListener(v -> finish());

        setupDateSelector();
        setupFlightList();

        searchFlights(fromCode, toCode, selectedDateStr);
    }

    private void setupDateSelector() {
        dateItems.clear();
        try {
            Date initialDate = apiDateFormat.parse(selectedDateStr);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            if (initialDate != null) cal.setTime(initialDate);

            for (int i = 0; i < 15; i++) {
                Date d = cal.getTime();
                dateItems.add(new DateSelectorAdapter.DateItem(d, -1, i == 0));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            Log.e("FlightResults", "Error parsing date", e);
        }

        dateAdapter = new DateSelectorAdapter(dateItems, date -> {
            selectedDateStr = apiDateFormat.format(date);
            searchFlights(fromCode, toCode, selectedDateStr);
        });
        
        binding.rvDateSelector.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvDateSelector.setAdapter(dateAdapter);
    }

    private void setupFlightList() {
        flightAdapter = new FlightAdapter(new ArrayList<>(), new FlightAdapter.OnFlightClickListener() {
            @Override
            public void onFlightClick(Flight flight) {
                navigateToFareSelection(flight);
            }

            @Override
            public void onDetailClick(Flight flight) {
                // TODO: Mở BottomSheet chi tiết chuyến bay
            }
        });
        binding.rvFlights.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFlights.setAdapter(flightAdapter);
    }

    private void navigateFareSelection(Flight flight) {
        Intent intent = new Intent(FlightResultsActivity.this, FareSelectionActivity.class);
        intent.putExtra("flight_json", gson.toJson(flight));
        startActivity(intent);
    }

    private void navigateToFareSelection(Flight flight) {
        navigateFareSelection(flight);
    }

    private void searchFlights(String from, String to, String date) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutNoResults.setVisibility(View.GONE);
        binding.rvFlights.setVisibility(View.GONE);

        FlightSearchRequest request = new FlightSearchRequest(from, to, date);
        RetrofitClient.getInstance().searchFlights(request).enqueue(new Callback<List<Flight>>() {
            @Override
            public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Flight> flights = response.body();

                    updateMinPriceOnSelector(flights);

                    if (flights.isEmpty()) {
                        binding.layoutNoResults.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvFlights.setVisibility(View.VISIBLE);
                        flightAdapter = new FlightAdapter(flights, new FlightAdapter.OnFlightClickListener() {
                            @Override
                            public void onFlightClick(Flight flight) {
                                navigateToFareSelection(flight);
                            }
                            @Override
                            public void onDetailClick(Flight flight) {
                                // TODO: Mở BottomSheet
                            }
                        });
                        binding.rvFlights.setAdapter(flightAdapter);
                    }
                } else {
                    binding.layoutNoResults.setVisibility(View.VISIBLE);
                    Toast.makeText(FlightResultsActivity.this, "Lỗi máy chủ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Flight>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutNoResults.setVisibility(View.VISIBLE);
                Toast.makeText(FlightResultsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMinPriceOnSelector(List<Flight> flights) {
        long localMin = Long.MAX_VALUE;
        boolean foundValid = false;
        for (Flight f : flights) {
            if (f.getBasePrice() > 0) {
                if (f.getBasePrice() < localMin) localMin = (long) f.getBasePrice();
                foundValid = true;
            }
        }
        
        long finalMin = foundValid ? localMin : 0;
        for (DateSelectorAdapter.DateItem item : dateItems) {
            if (apiDateFormat.format(item.date).equals(selectedDateStr)) {
                item.minPrice = (finalMin > 0) ? finalMin : 0;
                item.isSelected = true;
            } else {
                item.isSelected = false;
            }
        }
        if (dateAdapter != null) dateAdapter.notifyDataSetChanged();
    }
}
