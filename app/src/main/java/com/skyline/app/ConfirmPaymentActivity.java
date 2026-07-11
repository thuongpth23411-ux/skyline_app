package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityConfirmPaymentBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.Flight;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmPaymentActivity extends AppCompatActivity {

    private ActivityConfirmPaymentBinding binding;
    private double totalAmount;
    private final Gson gson = new Gson();
    private String passengerEmail, selectedSeat, fareType, passengerPhone, passengerDoc;
    private List<String> passengerNames;
    private Flight flight, returnFlight;
    private String returnSelectedSeat;
    private boolean isRoundTrip = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        initData();
        initViews();
    }

    private void initData() {
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("totalAmount", 0);
        passengerEmail = intent.getStringExtra("passenger_email");
        passengerNames = intent.getStringArrayListExtra("passenger_names");
        passengerPhone = intent.getStringExtra("passenger_phone");
        passengerDoc = intent.getStringExtra("passenger_doc");
        selectedSeat = intent.getStringExtra("selected_seat");
        fareType = intent.getStringExtra("fare_type");
        
        isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);

        String json = intent.getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
        }

        if (isRoundTrip) {
            String retJson = intent.getStringExtra("return_flight_json");
            if (retJson != null) returnFlight = gson.fromJson(retJson, Flight.class);
            returnSelectedSeat = intent.getStringExtra("returnSelectedSeat");
        }

        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvTotalPrice.setText(df.format(totalAmount) + " VND");
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup Card Type Spinner
        String[] cardTypes = {"Visa", "Mastercard", "JCB", "American Express"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cardTypes);
        binding.spinnerCardType.setAdapter(adapter);

        // Payment Method Selection
        binding.cardPaymentMethod.setOnClickListener(v -> toggleCardDetails(true));
        binding.vnpayMethod.setOnClickListener(v -> toggleCardDetails(false));
        binding.vietqrMethod.setOnClickListener(v -> toggleCardDetails(false));
        binding.momoMethod.setOnClickListener(v -> toggleCardDetails(false));

        binding.etExpiry.setOnClickListener(v -> showExpiryPicker());

        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnPay.setEnabled(isChecked);
            binding.btnPay.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        binding.btnPay.setOnClickListener(v -> processPayment());
    }

    private void toggleCardDetails(boolean show) {
        binding.cardDetails.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rbPaymentCard.setChecked(show);
        // Simple logic for radio buttons in sample
    }

    private void showExpiryPicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("CHỌN NGÀY HẾT HẠN")
                .setTheme(R.style.CustomDatePickerTheme)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(selection);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            String formattedMonth = month < 10 ? "0" + month : String.valueOf(month);
            String formattedYear = String.valueOf(year).substring(2);
            binding.etExpiry.setText(formattedMonth + "/" + formattedYear);
        });
        picker.show(getSupportFragmentManager(), "ExpiryPicker");
    }

    private void processPayment() {
        binding.btnPay.setEnabled(false);
        binding.btnPay.setText("ĐANG XỬ LÝ GIAO DỊCH...");

        if (passengerNames == null || passengerNames.isEmpty()) return;

        // Tách danh sách ghế (ví dụ: "21A, 21B" -> ["21A", "21B"])
        String[] seatsOut = selectedSeat != null ? selectedSeat.split(", ") : new String[]{"--"};
        String[] seatsRet = returnSelectedSeat != null ? returnSelectedSeat.split(", ") : new String[]{"--"};

        final int totalRequests = passengerNames.size() * (isRoundTrip ? 2 : 1);
        final int[] completedRequests = {0};

        for (int i = 0; i < passengerNames.size(); i++) {
            String pName = passengerNames.get(i);
            String pSeat = i < seatsOut.length ? seatsOut[i] : seatsOut[0];

            // 1. Đặt vé lượt đi
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("userId", sessionManager.fetchAuthToken());
            bookingData.put("flightId", flight.getId());
            bookingData.put("seatId", flight.getId() + "_" + pSeat);
            bookingData.put("passengerName", pName);
            bookingData.put("totalAmount", totalAmount / passengerNames.size()); // Chia đều tiền cho mỗi vé
            bookingData.put("ticketType", isRoundTrip ? "Departure" : "OneWay");
            bookingData.put("status", "Paid");

            RetrofitClient.getInstance().createBooking(bookingData).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    completedRequests[0]++;
                    checkAllCompleted(completedRequests[0], totalRequests);
                }
                @Override public void onFailure(Call<BaseResponse> call, Throwable t) {
                    completedRequests[0]++;
                    checkAllCompleted(completedRequests[0], totalRequests);
                }
            });

            // 2. Đặt vé lượt về (nếu có)
            if (isRoundTrip && returnFlight != null) {
                String pSeatRet = i < seatsRet.length ? seatsRet[i] : seatsRet[0];
                Map<String, Object> returnData = new HashMap<>(bookingData);
                returnData.put("flightId", returnFlight.getId());
                returnData.put("seatId", returnFlight.getId() + "_" + pSeatRet);
                returnData.put("ticketType", "Return");

                RetrofitClient.getInstance().createBooking(returnData).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        completedRequests[0]++;
                        checkAllCompleted(completedRequests[0], totalRequests);
                    }
                    @Override public void onFailure(Call<BaseResponse> call, Throwable t) {
                        completedRequests[0]++;
                        checkAllCompleted(completedRequests[0], totalRequests);
                    }
                });
            }
        }
    }

    private void checkAllCompleted(int current, int total) {
        if (current >= total) {
            navigateToSuccess();
        }
    }

    private void navigateToSuccess() {
        Toast.makeText(this, "Thanh toán thành công cho " + passengerNames.size() + " hành khách!", Toast.LENGTH_LONG).show();
        sessionManager.addLocalNotification("Đặt vé thành công", "Bạn đã đặt thành công vé cho đoàn " + passengerNames.size() + " người.");
        
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
        startActivity(intent);
        finish();
    }
}