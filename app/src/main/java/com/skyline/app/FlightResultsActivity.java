package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityFlightResultsBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSearchRequest;
import com.skyline.app.network.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightResultsActivity extends AppCompatActivity {

    private ActivityFlightResultsBinding binding;
    private FlightAdapter flightAdapter;
    private DateSelectorAdapter dateAdapter;
    private String fromCode, toCode, selectedDateStr;
    private final List<DateSelectorAdapter.DateItem> dateItems = new ArrayList<>();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private LinearLayoutManager dateLayoutManager;
    private final Gson gson = new Gson();
    
    private List<Flight> currentFlights = new ArrayList<>();
    private List<Flight> originalFlights = new ArrayList<>();
    private List<com.skyline.app.network.Airline> allAirlines = new ArrayList<>();
    private FlightFilter flightFilter = new FlightFilter();
    private int sortMode = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlightResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        fromCode = getIntent().getStringExtra("fromCode");
        toCode = getIntent().getStringExtra("toCode");
        selectedDateStr = getIntent().getStringExtra("date");
        String fromName = getIntent().getStringExtra("fromName");
        String toName = getIntent().getStringExtra("toName");

        binding.tvFromCity.setText((fromName != null && fromName.length() > 3) ? fromName : fromCode);
        binding.tvToCity.setText((toName != null && toName.length() > 3) ? toName : toCode);
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSortAsc.setOnClickListener(v -> toggleSort(1));
        binding.btnSortDesc.setOnClickListener(v -> toggleSort(2));
        binding.btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        setupDateSelector();
        setupFlightList();
        updateSortUI();
        loadAllAirlines();

        searchFlights(fromCode, toCode, selectedDateStr);
    }

    private void loadAllAirlines() {
        RetrofitClient.getInstance().getAirlines().enqueue(new Callback<List<com.skyline.app.network.Airline>>() {
            @Override
            public void onResponse(Call<List<com.skyline.app.network.Airline>> call, Response<List<com.skyline.app.network.Airline>> response) {
                if (response.isSuccessful() && response.body() != null) allAirlines = response.body();
            }
            @Override public void onFailure(Call<List<com.skyline.app.network.Airline>> call, Throwable t) {}
        });
    }

    private void toggleSort(int mode) {
        sortMode = (sortMode == mode) ? 0 : mode;
        applySort();
        updateSortUI();
    }

    private void showFilterBottomSheet() {
        FlightFilterBottomSheet sheet = new FlightFilterBottomSheet();
        sheet.setup(allAirlines, flightFilter, filter -> {
            this.flightFilter = filter;
            applySort();
        });
        sheet.show(getSupportFragmentManager(), "FilterSheet");
    }

    public void applySort() {
        if (originalFlights == null) return;
        currentFlights = new ArrayList<>();
        for (Flight f : originalFlights) {
            if (isFlightMatchesFilter(f)) {
                currentFlights.add(f);
            }
        }
        if (sortMode == 1) Collections.sort(currentFlights, (f1, f2) -> Double.compare(f1.getBasePrice(), f2.getBasePrice()));
        else if (sortMode == 2) Collections.sort(currentFlights, (f1, f2) -> Double.compare(f2.getBasePrice(), f1.getBasePrice()));
        
        flightAdapter = new FlightAdapter(currentFlights, new FlightAdapter.OnFlightClickListener() {
            @Override public void onFlightClick(Flight f) { navigateToFareSelection(f); }
            @Override public void onDetailClick(Flight f) { showFlightDetail(f); }
        });
        binding.rvFlights.setAdapter(flightAdapter);

        boolean empty = currentFlights.isEmpty();
        binding.layoutNoResults.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvFlights.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private boolean isFlightMatchesFilter(Flight f) {
        if (flightFilter == null || flightFilter.isEmpty()) return true;
        
        if (!flightFilter.airlineIds.isEmpty()) {
            if (f.getAirline() == null || !flightFilter.airlineIds.contains(f.getAirline().getName())) return false;
        }
        
        if (flightFilter.priceRangeIndex != -1) {
            double p = f.getBasePrice() / 1_000_000.0;
            if (flightFilter.priceRangeIndex == 0 && p >= 1.5) return false;
            if (flightFilter.priceRangeIndex == 1 && (p < 1.5 || p >= 2.5)) return false;
            if (flightFilter.priceRangeIndex == 2 && (p < 2.5 || p >= 4.0)) return false;
            if (flightFilter.priceRangeIndex == 3 && p < 4.0) return false;
        }
        
        if (flightFilter.timeSlotIndex != -1) {
            try {
                String depAt = f.getDepartureAt();
                int hour = -1;
                
                Pattern p = Pattern.compile("(\\d{2}):\\d{2}");
                Matcher m = p.matcher(depAt);
                if (m.find()) {
                    hour = Integer.parseInt(m.group(1));
                }

                if (hour != -1) {
                    switch(flightFilter.timeSlotIndex) {
                        case 0: if (hour >= 6) return false; break;
                        case 1: if (hour < 6 || hour >= 12) return false; break;
                        case 2: if (hour < 12 || hour >= 18) return false; break;
                        case 3: if (hour < 18) return false; break;
                    }
                }
            } catch (Exception e) {
                return true;
            }
        }
        
        if (flightFilter.durationIndex != -1) {
            int d = f.getDuration();
            if (flightFilter.durationIndex == 0 && d >= 60) return false;
            if (flightFilter.durationIndex == 1 && (d < 60 || d > 120)) return false;
            if (flightFilter.durationIndex == 2 && d <= 120) return false;
        }
        
        return true;
    }

    private void updateSortUI() {
        int active = ContextCompat.getColor(this, R.color.skyline_blue_dark);
        int white = ContextCompat.getColor(this, R.color.white);
        binding.btnSortDesc.setBackgroundColor(sortMode == 2 ? active : white);
        binding.btnSortDesc.setTextColor(sortMode == 2 ? white : active);
        binding.btnSortAsc.setBackgroundColor(sortMode == 1 ? active : white);
        binding.btnSortAsc.setTextColor(sortMode == 1 ? white : active);
    }

    private void setupDateSelector() {
        dateItems.clear();
        int initialPos = 0;
        try {
            Date initialDate = apiDateFormat.parse(selectedDateStr);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            if (initialDate != null) { cal.setTime(initialDate); cal.add(Calendar.DAY_OF_MONTH, -10); }
            for (int i = 0; i < 30; i++) {
                Date d = cal.getTime();
                String dStr = apiDateFormat.format(d);
                if (dStr.equals(selectedDateStr)) initialPos = i;
                dateItems.add(new DateSelectorAdapter.DateItem(d, 0, dStr.equals(selectedDateStr)));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {}

        dateAdapter = new DateSelectorAdapter(dateItems, (date, pos) -> {
            centerItem(pos, true);
            selectedDateStr = apiDateFormat.format(date);
            searchFlights(fromCode, toCode, selectedDateStr);
        });
        dateLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvDateSelector.setLayoutManager(dateLayoutManager);
        binding.rvDateSelector.setAdapter(dateAdapter);
        
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(binding.rvDateSelector);

        int finalPos = initialPos;
        binding.rvDateSelector.post(() -> {
            int width = binding.rvDateSelector.getWidth();
            int itemWidth = (int)(130 * getResources().getDisplayMetrics().density); 
            int padding = width / 2 - itemWidth / 2;
            binding.rvDateSelector.setPadding(padding, 0, padding, 0);
            binding.rvDateSelector.setClipToPadding(false);
            dateLayoutManager.scrollToPositionWithOffset(finalPos, 0);
        });

        binding.rvDateSelector.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(dateLayoutManager);
                    if (centerView != null) {
                        int pos = dateLayoutManager.getPosition(centerView);
                        if (pos != RecyclerView.NO_POSITION && pos < dateItems.size()) {
                            String newDate = apiDateFormat.format(dateItems.get(pos).date);
                            if (!newDate.equals(selectedDateStr)) {
                                selectedDateStr = newDate;
                                dateAdapter.updateSelection(pos);
                                searchFlights(fromCode, toCode, selectedDateStr);
                            }
                        }
                    }
                }
            }
        });

        fetchAllPrices();
    }

    private void centerItem(int pos, boolean anim) {
        dateAdapter.updateSelection(pos);
        if (anim) binding.rvDateSelector.smoothScrollToPosition(pos);
        else dateLayoutManager.scrollToPositionWithOffset(pos, 0);
    }

    private void fetchAllPrices() {
        for (int i = 0; i < dateItems.size(); i++) {
            final int pos = i;
            String date = apiDateFormat.format(dateItems.get(pos).date);
            RetrofitClient.getInstance().searchFlights(new FlightSearchRequest(fromCode, toCode, date)).enqueue(new Callback<List<Flight>>() {
                @Override public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        long min = Long.MAX_VALUE;
                        for (Flight f : response.body()) {
                            if (f.getBasePrice() > 0 && f.getBasePrice() < min) min = (long) f.getBasePrice();
                        }
                        if (min != Long.MAX_VALUE) {
                            dateItems.get(pos).minPrice = min;
                            dateAdapter.notifyItemChanged(pos);
                        }
                    }
                }
                @Override public void onFailure(Call<List<Flight>> call, Throwable t) {}
            });
        }
    }

    private void setupFlightList() {
        flightAdapter = new FlightAdapter(new ArrayList<>(), new FlightAdapter.OnFlightClickListener() {
            @Override public void onFlightClick(Flight f) { navigateToFareSelection(f); }
            @Override public void onDetailClick(Flight f) {}
        });
        binding.rvFlights.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFlights.setAdapter(flightAdapter);
    }

    private void showFlightDetail(Flight f) {
        FlightDetailBottomSheet sheet = FlightDetailBottomSheet.newInstance(f, this::navigateToFareSelection);
        sheet.show(getSupportFragmentManager(), "FlightDetailSheet");
    }

    private void navigateToFareSelection(Flight f) {
        Intent i = new Intent(this, FareSelectionActivity.class);
        // TRUYỀN DỮ LIỆU DƯỚI DẠNG JSON ĐÚNG NHƯ TRANG SAU YÊU CẦU
        i.putExtra("flight_json", gson.toJson(f));
        startActivity(i);
    }

    private void searchFlights(String from, String to, String date) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutNoResults.setVisibility(View.GONE);
        binding.rvFlights.setVisibility(View.GONE);

        RetrofitClient.getInstance().searchFlights(new FlightSearchRequest(from, to, date)).enqueue(new Callback<List<Flight>>() {
            @Override public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    originalFlights = response.body();
                    if (!originalFlights.isEmpty()) {
                        Flight f = originalFlights.get(0);
                        if (f.getDepartureAirport() != null && f.getDepartureAirport().getName() != null) binding.tvFromCity.setText(f.getDepartureAirport().getName());
                        if (f.getArrivalAirport() != null && f.getArrivalAirport().getName() != null) binding.tvToCity.setText(f.getArrivalAirport().getName());
                    }
                    applySort();
                }
            }
            @Override public void onFailure(Call<List<Flight>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
