package com.skyline.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skyline.app.databinding.FragmentBookBinding;
import com.skyline.app.network.Airport;
import com.skyline.app.network.RecentSearch;
import com.skyline.app.network.RetrofitClient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookFragment extends Fragment {

    private FragmentBookBinding binding;
    private String fromCode = "", fromCity = "Chọn điểm đi";
    private String toCode = "", toCity = "Chọn điểm đến";
    private long departureDate = 0;
    private long returnDate = 0;
    private int adults = 1, children = 0, infants = 0;
    private boolean isRoundTrip = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private final Gson gson = new Gson();

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                if ((fine != null && fine) || (coarse != null && coarse)) {
                    determineNearestAirport();
                }
            }
    );

    private final ActivityResultLauncher<Intent> airportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    boolean isFrom = result.getData().getBooleanExtra("isFrom", true);
                    String code = result.getData().getStringExtra("code");
                    String city = result.getData().getStringExtra("city");
                    if (isFrom) {
                        if (code.equals(toCode)) {
                            Toast.makeText(requireContext(), "Điểm đi không được trùng với điểm đến", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        fromCode = code;
                        fromCity = city;
                        binding.tvFromCode.setText(code);
                        binding.tvFromCity.setText(city);
                        binding.tvFromCode.setTextSize(32);
                    } else {
                        if (code.equals(fromCode)) {
                            Toast.makeText(requireContext(), "Điểm đến không được trùng với điểm đi", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        toCode = code;
                        toCity = city;
                        binding.tvToCode.setText(code);
                        binding.tvToCity.setText(city);
                        binding.tvToCode.setTextSize(32);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        loadRecentSearches();
        checkLocationPermission();
    }

    private void setupUI() {
        updateTabUI();
        updateDateUI();
        updatePassengersDisplay();

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                getActivity().findViewById(R.id.navHome).performClick();
            }
        });

        binding.toggleTripType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isRoundTrip = (checkedId == R.id.btnRoundTrip);
                updateTabUI();
                updateDateUI();
            }
        });

        binding.layoutFrom.setOnClickListener(v -> openAirportSelection(true));
        binding.layoutTo.setOnClickListener(v -> openAirportSelection(false));

        binding.btnSwap.setOnClickListener(v -> {
            if (fromCode.isEmpty() || toCode.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn đủ điểm đi và điểm đến", Toast.LENGTH_SHORT).show();
                return;
            }
            String tempCode = fromCode;
            String tempCity = fromCity;
            fromCode = toCode;
            fromCity = toCity;
            toCode = tempCode;
            toCity = tempCity;

            binding.tvFromCode.setText(fromCode);
            binding.tvFromCity.setText(fromCity);
            binding.tvToCode.setText(toCode);
            binding.tvToCity.setText(toCity);
        });

        // Departure Date Clicks
        binding.ivDepCalendar.setOnClickListener(v -> showDatePicker(true));
        setupManualDateEntry(binding.tvDepDate, true);

        // Return Date Clicks
        binding.ivReturnCalendar.setOnClickListener(v -> {
            if (isRoundTrip) showDatePicker(false);
        });
        setupManualDateEntry(binding.tvReturnDate, false);

        binding.btnSelectPassengers.setOnClickListener(v -> {
            PassengerBottomSheet sheet = new PassengerBottomSheet(adults, children, infants, (a, c, i) -> {
                adults = a; children = c; infants = i;
                updatePassengersDisplay();
            });
            sheet.show(getChildFragmentManager(), "PassengerBottomSheet");
        });

        binding.btnSearch.setOnClickListener(v -> {
            if (fromCode.isEmpty() || toCode.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn đủ điểm đi và điểm đến", Toast.LENGTH_SHORT).show();
                return;
            }
            if (departureDate == 0) {
                Toast.makeText(requireContext(), "Vui lòng chọn ngày đi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isRoundTrip && returnDate == 0) {
                Toast.makeText(requireContext(), "Vui lòng chọn ngày về", Toast.LENGTH_SHORT).show();
                return;
            }
            
            saveRecentSearch();
            
            Intent intent = new Intent(requireContext(), FlightResultsActivity.class);
            intent.putExtra("fromCode", fromCode);
            intent.putExtra("toCode", toCode);
            intent.putExtra("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(departureDate)));
            startActivity(intent);
        });
    }

    private void setupManualDateEntry(android.widget.EditText editText, boolean isDeparture) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() == 10) {
                    try {
                        Date date = dateFormat.parse(input);
                        if (date != null) {
                            long time = date.getTime();
                            if (isDeparture) {
                                departureDate = time;
                                binding.tvDepDayOfWeek.setText(dayFormat.format(date));
                            } else {
                                if (time < departureDate) {
                                    Toast.makeText(requireContext(), "Ngày về không được nhỏ hơn ngày đi", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                returnDate = time;
                                binding.tvReturnDayOfWeek.setText(dayFormat.format(date));
                            }
                        }
                    } catch (ParseException e) {
                        // handled by updateDateUI on loss of focus or search
                    }
                }
            }
        });
    }

    private void openAirportSelection(boolean isFrom) {
        Intent intent = new Intent(requireContext(), AirportSelectionActivity.class);
        intent.putExtra("title", isFrom ? "Chọn điểm đi" : "Chọn điểm đến");
        intent.putExtra("isFrom", isFrom);
        airportLauncher.launch(intent);
    }

    private void updatePassengersDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(adults).append(" Người lớn");
        if (children > 0) sb.append(", ").append(children).append(" Trẻ em");
        if (infants > 0) sb.append(", ").append(infants).append(" Em bé");
        binding.tvPassengers.setText(sb.toString());
    }

    private void updateTabUI() {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark);
        int activeBg = ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark);
        int inactiveBg = android.graphics.Color.parseColor("#E5E7EB");

        android.content.res.ColorStateList activeList = android.content.res.ColorStateList.valueOf(activeBg);
        android.content.res.ColorStateList inactiveList = android.content.res.ColorStateList.valueOf(inactiveBg);

        if (isRoundTrip) {
            binding.btnRoundTrip.setBackgroundTintList(activeList);
            binding.btnRoundTrip.setTextColor(activeColor);
            binding.btnOneWay.setBackgroundTintList(inactiveList);
            binding.btnOneWay.setTextColor(inactiveColor);
        } else {
            binding.btnOneWay.setBackgroundTintList(activeList);
            binding.btnOneWay.setTextColor(activeColor);
            binding.btnRoundTrip.setBackgroundTintList(inactiveList);
            binding.btnRoundTrip.setTextColor(inactiveColor);
        }
    }

    private void updateDateUI() {
        if (departureDate == 0) {
            binding.ivDepCalendar.setVisibility(View.VISIBLE);
            binding.tvDepDate.setVisibility(View.GONE);
            binding.tvDepDayOfWeek.setText("Chọn ngày đi");
            binding.tvDepDayOfWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark));
        } else {
            binding.ivDepCalendar.setVisibility(View.VISIBLE);
            binding.tvDepDate.setVisibility(View.VISIBLE);
            binding.tvDepDate.setText(dateFormat.format(new Date(departureDate)));
            binding.tvDepDayOfWeek.setText(dayFormat.format(new Date(departureDate)));
            binding.tvDepDayOfWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
        }

        if (isRoundTrip) {
            binding.dateDivider.setVisibility(View.VISIBLE);
            binding.btnSelectReturnDate.setVisibility(View.VISIBLE);
            if (returnDate == 0) {
                binding.ivReturnCalendar.setVisibility(View.VISIBLE);
                binding.tvReturnDate.setVisibility(View.GONE);
                binding.tvReturnDayOfWeek.setText("Chọn ngày về");
                binding.tvReturnDayOfWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark));
            } else {
                binding.ivReturnCalendar.setVisibility(View.VISIBLE);
                binding.tvReturnDate.setVisibility(View.VISIBLE);
                binding.tvReturnDate.setText(dateFormat.format(new Date(returnDate)));
                binding.tvReturnDayOfWeek.setText(dayFormat.format(new Date(returnDate)));
                binding.tvReturnDayOfWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
            }
        } else {
            binding.dateDivider.setVisibility(View.GONE);
            binding.btnSelectReturnDate.setVisibility(View.GONE);
        }
    }

    private void showDatePicker(boolean isDeparture) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowStart = cal.getTimeInMillis();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(tomorrowStart))
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isDeparture ? "Chọn ngày đi" : "Chọn ngày về")
                .setCalendarConstraints(constraints)
                .setTheme(R.style.CustomDatePickerTheme)
                // [XÓA THANH THỪA]: Ép buộc chế độ lịch, kết hợp với Theme sẽ ẩn hoàn toàn thanh icon sửa
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setSelection(isDeparture ? (departureDate != 0 ? departureDate : tomorrowStart) : (returnDate != 0 ? returnDate : (departureDate != 0 ? departureDate : tomorrowStart)))
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isDeparture) {
                departureDate = selection;
                if (isRoundTrip && returnDate != 0 && returnDate < departureDate) returnDate = 0;
            } else {
                if (selection < departureDate) {
                    Toast.makeText(requireContext(), "Ngày về không được nhỏ hơn ngày đi", Toast.LENGTH_SHORT).show();
                    return;
                }
                returnDate = selection;
            }
            updateDateUI();
        });
        picker.show(getChildFragmentManager(), "DatePicker");
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            determineNearestAirport();
        }
    }

    private void determineNearestAirport() {
        RetrofitClient.getInstance().getAirports().enqueue(new Callback<List<Airport>>() {
            @Override
            public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Airport first = response.body().get(0);
                    fromCode = first.getCode();
                    fromCity = first.getCity();
                    if (binding != null) {
                        binding.tvFromCode.setText(fromCode);
                        binding.tvFromCity.setText(fromCity);
                        binding.tvFromCode.setTextSize(32);
                        binding.tvFromCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Airport>> call, Throwable t) {}
        });
    }

    private void saveRecentSearch() {
        SharedPreferences prefs = requireContext().getSharedPreferences("SkylinePrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("recent_searches", "[]");
        List<RecentSearch> list = gson.fromJson(json, new TypeToken<List<RecentSearch>>(){}.getType());
        
        RecentSearch search = new RecentSearch();
        search.fromAirportId = fromCode;
        search.fromCity = fromCity;
        search.toAirportId = toCode;
        search.toCity = toCity;
        search.departureDate = dateFormat.format(new Date(departureDate));
        search.returnDate = isRoundTrip ? (returnDate != 0 ? dateFormat.format(new Date(returnDate)) : null) : null;
        search.isRoundTrip = isRoundTrip;
        search.adults = adults;
        search.children = children;
        search.infants = infants;
        search.createdAt = System.currentTimeMillis();

        list.add(0, search);
        if (list.size() > 5) list = list.subList(0, 5);
        prefs.edit().putString("recent_searches", gson.toJson(list)).apply();
        loadRecentSearches();
    }

    private void loadRecentSearches() {
        SharedPreferences prefs = requireContext().getSharedPreferences("SkylinePrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("recent_searches", "[]");
        List<RecentSearch> list = gson.fromJson(json, new TypeToken<List<RecentSearch>>(){}.getType());

        if (list.isEmpty()) {
            binding.tvRecentTitle.setVisibility(View.GONE);
            binding.rvRecentSearches.setVisibility(View.GONE);
        } else {
            if (list.size() > 5) list = list.subList(0, 5);
            
            binding.tvRecentTitle.setVisibility(View.VISIBLE);
            binding.rvRecentSearches.setVisibility(View.VISIBLE);
            binding.rvRecentSearches.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvRecentSearches.setAdapter(new RecentSearchAdapter(list, item -> {
                fromCode = item.fromAirportId;
                fromCity = item.fromCity;
                toCode = item.toAirportId;
                toCity = item.toCity;
                isRoundTrip = item.isRoundTrip;
                adults = item.adults;
                children = item.children;
                infants = item.infants;
                
                try {
                    Date dDate = dateFormat.parse(item.departureDate);
                    if (dDate != null) departureDate = dDate.getTime();
                    
                    if (item.returnDate != null) {
                        Date rDate = dateFormat.parse(item.returnDate);
                        if (rDate != null) returnDate = rDate.getTime();
                    } else {
                        returnDate = 0;
                    }
                } catch (Exception e) {
                    departureDate = 0;
                    returnDate = 0;
                }
                
                binding.tvFromCode.setText(fromCode);
                binding.tvFromCity.setText(fromCity);
                binding.tvToCode.setText(toCode);
                binding.tvToCity.setText(toCity);
                binding.tvToCode.setTextSize(32);
                binding.tvFromCode.setTextSize(32);
                binding.toggleTripType.check(isRoundTrip ? R.id.btnRoundTrip : R.id.btnOneWay);
                
                updatePassengersDisplay();
                updateTabUI();
                updateDateUI();
            }));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
