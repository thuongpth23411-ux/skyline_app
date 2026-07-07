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
    private String fromCode = "", fromCity = "Chọn điểm đi", fromError = "";
    private String toCode = "", toCity = "Chọn điểm đến", toError = "";
    private long departureDate = 0;
    private long returnDate = 0;
    private int adults = 1, children = 0, infants = 0;
    private boolean isRoundTrip = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private final Gson gson = new Gson();

    public BookFragment() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
    }

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                if ((fine != null && fine) || (coarse != null && coarse)) {
                    determineNearestAirport();
                } else {
                    loadInitialAirports();
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
                    if (code == null) code = "";
                    if (city == null) city = "";
                    
                    if (isFrom) {
                        if (code.equals(toCode) && !code.isEmpty()) {
                            fromError = "Điểm đi không được trùng với điểm đến";
                            fromCode = "";
                            updateAirportDisplay();
                            return;
                        }
                        fromCode = code;
                        fromCity = city;
                        fromError = "";
                        updateAirportDisplay();
                    } else {
                        if (code.equals(fromCode) && !code.isEmpty()) {
                            toError = "Điểm đến không được trùng với điểm đi";
                            toCode = "";
                            updateAirportDisplay();
                            return;
                        }
                        toCode = code;
                        toCity = city;
                        toError = "";
                        updateAirportDisplay();
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
        updateAirportDisplay();

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
                if (fromCode.isEmpty()) fromError = "Vui lòng chọn điểm đi";
                if (toCode.isEmpty()) toError = "Vui lòng chọn điểm đến";
                updateAirportDisplay();
                return;
            }
            String tempCode = fromCode;
            String tempCity = fromCity;
            fromCode = toCode;
            fromCity = toCity;
            toCode = tempCode;
            toCity = tempCity;
            
            fromError = "";
            toError = "";
            updateAirportDisplay();
        });

        binding.ivDepCalendar.setOnClickListener(v -> showDatePicker(true));
        binding.ivReturnCalendar.setOnClickListener(v -> {
            if (isRoundTrip) showDatePicker(false);
        });

        setupManualDateEntry(binding.tvDepDate, true);
        setupManualDateEntry(binding.tvReturnDate, false);

        binding.btnSelectPassengers.setOnClickListener(v -> {
            PassengerBottomSheet sheet = new PassengerBottomSheet(adults, children, infants, (a, c, i) -> {
                adults = a; children = c; infants = i;
                updatePassengersDisplay();
            });
            sheet.show(getChildFragmentManager(), "PassengerBottomSheet");
        });

        binding.btnSearch.setOnClickListener(v -> {
            boolean hasAirportError = false;
            
            if (fromCode.isEmpty()) {
                fromError = "Vui lòng chọn điểm đi";
                hasAirportError = true;
            }
            if (toCode.isEmpty()) {
                toError = "Vui lòng chọn điểm đến";
                hasAirportError = true;
            }

            if (hasAirportError) {
                updateAirportDisplay();
            }

            syncDatesFromInput();
            
            if (departureDate == 0) {
                binding.tvDepError.setText("Vui lòng chọn ngày khởi hành");
                binding.tvDepError.setVisibility(View.VISIBLE);
            }

            if (isRoundTrip && returnDate == 0) {
                binding.tvReturnError.setText("Vui lòng chọn ngày khứ hồi");
                binding.tvReturnError.setVisibility(View.VISIBLE);
            }

            if (hasAirportError || 
                !fromError.isEmpty() || 
                !toError.isEmpty() ||
                binding.tvDepError.getVisibility() == View.VISIBLE || 
                (isRoundTrip && binding.tvReturnError.getVisibility() == View.VISIBLE) ||
                departureDate == 0 ||
                (isRoundTrip && returnDate == 0)) {
                return;
            }

            long tomorrowStart = getTomorrowStartUtc();
            if (departureDate < tomorrowStart) {
                binding.tvDepError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                binding.tvDepError.setVisibility(View.VISIBLE);
                return;
            }

            if (isRoundTrip && returnDate < departureDate) {
                binding.tvReturnError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                binding.tvReturnError.setVisibility(View.VISIBLE);
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

    private void updateAirportDisplay() {
        if (!fromError.isEmpty()) {
            binding.tvFromCode.setText("+");
            binding.tvFromCity.setText(fromError);
            binding.tvFromCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.auth_error));
            binding.tvFromCity.setTextSize(10); 
        } else if (fromCode.isEmpty()) {
            binding.tvFromCode.setText("+");
            binding.tvFromCity.setText("Chọn điểm đi");
            binding.tvFromCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
            binding.tvFromCity.setTextSize(14); 
        } else {
            binding.tvFromCode.setText(fromCode);
            binding.tvFromCity.setText(fromCity);
            binding.tvFromCode.setTextSize(32);
            binding.tvFromCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark));
            binding.tvFromCity.setTextSize(14); 
        }

        if (!toError.isEmpty()) {
            binding.tvToCode.setText("+");
            binding.tvToCity.setText(toError);
            binding.tvToCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.auth_error));
            binding.tvToCity.setTextSize(10); 
        } else if (toCode.isEmpty()) {
            binding.tvToCode.setText("+");
            binding.tvToCity.setText("Chọn điểm đến");
            binding.tvToCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_text_secondary));
            binding.tvToCity.setTextSize(14); 
        } else {
            binding.tvToCode.setText(toCode);
            binding.tvToCity.setText(toCity);
            binding.tvToCode.setTextSize(32);
            binding.tvToCity.setTextColor(ContextCompat.getColor(requireContext(), R.color.skyline_blue_dark));
            binding.tvToCity.setTextSize(14); 
        }
    }

    private long getTomorrowStartUtc() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    private void syncDatesFromInput() {
        try {
            String depStr = binding.tvDepDate.getText().toString();
            if (depStr.length() == 10) {
                Date d = dateFormat.parse(depStr);
                if (d != null) departureDate = d.getTime();
            }
            
            if (isRoundTrip) {
                String retStr = binding.tvReturnDate.getText().toString();
                if (retStr.length() == 10) {
                    Date r = dateFormat.parse(retStr);
                    if (r != null) returnDate = r.getTime();
                }
            }
        } catch (ParseException e) {
            Log.e("BookFragment", "Parse error in sync");
        }
    }

    private void setupManualDateEntry(android.widget.EditText editText, boolean isDeparture) {
        editText.addTextChangedListener(new TextWatcher() {
            private String prevText = "";

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevText = s.toString();
            }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() > prevText.length()) {
                    if (input.length() == 2 || input.length() == 5) {
                        s.append("/");
                    }
                }

                if (input.length() == 10) {
                    try {
                        Date date = dateFormat.parse(input);
                        if (date != null) {
                            long time = date.getTime();
                            long tomorrow = getTomorrowStartUtc();
                            
                            if (isDeparture) {
                                if (time < tomorrow) {
                                    binding.tvDepError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                                    binding.tvDepError.setVisibility(View.VISIBLE);
                                } else {
                                    binding.tvDepError.setVisibility(View.GONE);
                                    departureDate = time;
                                }
                            } else {
                                if (departureDate != 0 && time < departureDate) {
                                    binding.tvReturnError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                                    binding.tvReturnError.setVisibility(View.VISIBLE);
                                } else if (time < tomorrow) {
                                    binding.tvReturnError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                                    binding.tvReturnError.setVisibility(View.VISIBLE);
                                } else {
                                    binding.tvReturnError.setVisibility(View.GONE);
                                    returnDate = time;
                                }
                            }
                        }
                    } catch (ParseException e) {
                    }
                } else {
                    if (isDeparture) binding.tvDepError.setVisibility(View.GONE);
                    else binding.tvReturnError.setVisibility(View.GONE);
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
            binding.tvDepDate.setText("");
        } else {
            binding.tvDepDate.setText(dateFormat.format(new Date(departureDate)));
        }

        if (isRoundTrip) {
            binding.dateDivider.setVisibility(View.VISIBLE);
            binding.btnSelectReturnDate.setVisibility(View.VISIBLE);
            if (returnDate == 0) {
                binding.tvReturnDate.setText("");
            } else {
                binding.tvReturnDate.setText(dateFormat.format(new Date(returnDate)));
            }
        } else {
            binding.dateDivider.setVisibility(View.GONE);
            binding.btnSelectReturnDate.setVisibility(View.GONE);
        }
    }

    private void showDatePicker(boolean isDeparture) {
        long tomorrowStart = getTomorrowStartUtc();
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(tomorrowStart))
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isDeparture ? "Chọn ngày đi" : "Chọn ngày về")
                .setCalendarConstraints(constraints)
                .setTheme(R.style.CustomDatePickerTheme)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setSelection(isDeparture ? (departureDate != 0 ? departureDate : tomorrowStart) : (returnDate != 0 ? returnDate : (departureDate != 0 ? departureDate : tomorrowStart)))
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isDeparture) {
                departureDate = selection;
                binding.tvDepError.setVisibility(View.GONE);
                if (isRoundTrip && returnDate != 0 && returnDate < departureDate) {
                    returnDate = 0;
                    binding.tvReturnError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                    binding.tvReturnError.setVisibility(View.VISIBLE);
                }
            } else {
                if (selection < departureDate) {
                    binding.tvReturnError.setText("Ngày bay không khả dụng, Quý khách vui lòng chọn lại ngày bay mới.");
                    binding.tvReturnError.setVisibility(View.VISIBLE);
                    return;
                }
                returnDate = selection;
                binding.tvReturnError.setVisibility(View.GONE);
            }
            updateDateUI();
        });
        picker.show(getChildFragmentManager(), "DatePicker");
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
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
                    updateAirportDisplay();
                }
            }
            @Override
            public void onFailure(Call<List<Airport>> call, Throwable t) {
                loadInitialAirports();
            }
        });
    }

    private void loadInitialAirports() {
        RetrofitClient.getInstance().getAirports().enqueue(new Callback<List<Airport>>() {
            @Override
            public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Airport first = response.body().get(0);
                    fromCode = first.getCode();
                    fromCity = first.getCity();
                    updateAirportDisplay();
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

        list.removeIf(item -> 
            item.fromAirportId.equals(search.fromAirportId) &&
            item.toAirportId.equals(search.toAirportId) &&
            item.departureDate.equals(search.departureDate) &&
            ((item.returnDate == null && search.returnDate == null) || (item.returnDate != null && item.returnDate.equals(search.returnDate))) &&
            item.isRoundTrip == search.isRoundTrip &&
            item.adults == search.adults &&
            item.children == search.children &&
            item.infants == search.infants
        );

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
                
                updateAirportDisplay();
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
