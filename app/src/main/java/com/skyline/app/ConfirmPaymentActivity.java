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
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ConfirmPaymentActivity extends AppCompatActivity {

    private ActivityConfirmPaymentBinding binding;
    private double totalAmount;
    private final Gson gson = new Gson();
    private String passengerEmail, passengerName, selectedSeat, fareType;
    private Flight flight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initViews();
    }

    private void initData() {
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("totalAmount", 0);
        passengerEmail = intent.getStringExtra("passenger_email");
        passengerName = intent.getStringExtra("passenger_name");
        selectedSeat = intent.getStringExtra("selected_seat");
        fareType = intent.getStringExtra("fare_type");
        
        String json = intent.getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
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
        binding.rbVNPay.setChecked(!show && binding.vnpayMethod.isPressed());
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

        // GIẢ LẬP GỌI API BACKEND ĐỂ LƯU BOOKING
        Toast.makeText(this, "Thanh toán thành công! Vé đã được gửi tới: " + passengerEmail, Toast.LENGTH_LONG).show();

        binding.btnPay.postDelayed(() -> {
//            Intent intent = new Intent(this, SuccessActivity.class);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }
}