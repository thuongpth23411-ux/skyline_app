package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityConfirmPaymentBinding;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ConfirmPaymentActivity extends AppCompatActivity {

    private ActivityConfirmPaymentBinding binding;
    private double totalAmount;
    private final Gson gson = new Gson();
    private String passengerEmail, passengerName, selectedSeat, fareType;
    private Flight flight;
    private String selectedMethodName = "";
    private double baseFare = 0, taxes = 0, seatPrice = 0, addonPrice = 0;

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
        
        // Try to get detailed prices for the dialog
        baseFare = intent.getDoubleExtra("baseFare", totalAmount * 0.8);
        taxes = intent.getDoubleExtra("taxes", totalAmount * 0.1);
        seatPrice = intent.getDoubleExtra("seatPrice", 0);
        addonPrice = intent.getDoubleExtra("addonPrice", 0);

        String json = intent.getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
        }

        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvTotalPrice.setText(df.format(totalAmount) + " VND");
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvPriceDetail.setOnClickListener(v -> showPriceDetails());

        // Setup Card Type Spinner
        String[] cardTypes = {"Visa", "Mastercard", "JCB", "American Express"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cardTypes);
        binding.spinnerCardType.setAdapter(adapter);

        // Payment Method Selection
        binding.cardPaymentMethod.setOnClickListener(v -> selectPaymentMethod("card"));
        binding.vnpayMethod.setOnClickListener(v -> selectPaymentMethod("vnpay"));
        binding.vietqrMethod.setOnClickListener(v -> selectPaymentMethod("vietqr"));
        binding.momoMethod.setOnClickListener(v -> selectPaymentMethod("momo"));

        binding.etExpiry.setOnClickListener(v -> showExpiryPicker());

        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnPay.setEnabled(isChecked);
            binding.btnPay.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        binding.btnPay.setOnClickListener(v -> processPayment());

        setupCardNumberFormatting();

        binding.etCardHolder.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.AllCaps()});

        // Không chọn mặc định phương thức nào
        clearPaymentSelections();
    }

    private void setupCardNumberFormatting() {
        binding.etCardNumber.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                // Remove all non-digits
                String digits = s.toString().replaceAll("\\D", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(digits.charAt(i));
                }

                binding.etCardNumber.setText(formatted.toString());
                binding.etCardNumber.setSelection(formatted.length());
                isFormatting = false;
            }
        });
    }

    private void clearPaymentSelections() {
        binding.rbPaymentCard.setChecked(false);
        binding.rbVNPay.setChecked(false);
        binding.rbVietQR.setChecked(false);
        binding.rbMomo.setChecked(false);

        binding.cardDetails.setVisibility(View.GONE);
        binding.tvVNPayRedirect.setVisibility(View.GONE);
        binding.tvVietQRRedirect.setVisibility(View.GONE);
        binding.tvMomoRedirect.setVisibility(View.GONE);

        // Reset strokes - Viền xám nhạt mặc định
        int defaultStrokeColor = android.graphics.Color.parseColor("#E5E7EB");
        int defaultStrokeWidth = (int) (1.0 * getResources().getDisplayMetrics().density);

        binding.cardPaymentMethod.setStrokeColor(defaultStrokeColor);
        binding.cardPaymentMethod.setStrokeWidth(defaultStrokeWidth);
        binding.vnpayMethod.setStrokeColor(defaultStrokeColor);
        binding.vnpayMethod.setStrokeWidth(defaultStrokeWidth);
        binding.vietqrMethod.setStrokeColor(defaultStrokeColor);
        binding.vietqrMethod.setStrokeWidth(defaultStrokeWidth);
        binding.momoMethod.setStrokeColor(defaultStrokeColor);
        binding.momoMethod.setStrokeWidth(defaultStrokeWidth);
    }

    private void selectPaymentMethod(String method) {
        clearPaymentSelections();
        this.selectedMethodName = method;

        binding.rbPaymentCard.setChecked("card".equals(method));
        binding.rbVNPay.setChecked("vnpay".equals(method));
        binding.rbVietQR.setChecked("vietqr".equals(method));
        binding.rbMomo.setChecked("momo".equals(method));

        binding.cardDetails.setVisibility("card".equals(method) ? View.VISIBLE : View.GONE);
        binding.tvVNPayRedirect.setVisibility("vnpay".equals(method) ? View.VISIBLE : View.GONE);
        binding.tvVietQRRedirect.setVisibility("vietqr".equals(method) ? View.VISIBLE : View.GONE);
        binding.tvMomoRedirect.setVisibility("momo".equals(method) ? View.VISIBLE : View.GONE);

        // Highlight selected - Viền đen
        int strokeColor = android.graphics.Color.BLACK;
        int strokeWidth = (int) (1.5 * getResources().getDisplayMetrics().density);

        if ("card".equals(method)) {
            binding.cardPaymentMethod.setStrokeColor(strokeColor);
            binding.cardPaymentMethod.setStrokeWidth(strokeWidth);
        } else if ("vnpay".equals(method)) {
            binding.vnpayMethod.setStrokeColor(strokeColor);
            binding.vnpayMethod.setStrokeWidth(strokeWidth);
        } else if ("vietqr".equals(method)) {
            binding.vietqrMethod.setStrokeColor(strokeColor);
            binding.vietqrMethod.setStrokeWidth(strokeWidth);
        } else if ("momo".equals(method)) {
            binding.momoMethod.setStrokeColor(strokeColor);
            binding.momoMethod.setStrokeWidth(strokeWidth);
        }
    }

    private void showPriceDetails() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_price_detail, null);
        dialog.setContentView(view);
        
        DecimalFormat df = new DecimalFormat("#,###");

        TextView tvAdultPriceTotal = view.findViewById(R.id.tvAdultPriceTotal);
        TextView tvPricePerPerson = view.findViewById(R.id.tvPricePerPerson);
        TextView tvBaseFareValue = view.findViewById(R.id.tvBaseFareValue);
        TextView tvTaxFeesTotal = view.findViewById(R.id.tvTaxFeesTotal);
        TextView tvSeatFeeValue = view.findViewById(R.id.tvSeatFeeValue);
        TextView tvAddonFeeValue = view.findViewById(R.id.tvAddonFeeValue);
        TextView tvGrandTotal = view.findViewById(R.id.tvGrandTotal);

        if (tvAdultPriceTotal != null) tvAdultPriceTotal.setText(df.format(baseFare) + " VND");
        if (tvPricePerPerson != null) tvPricePerPerson.setText(df.format(baseFare) + " VND/Người");
        if (tvBaseFareValue != null) tvBaseFareValue.setText(df.format(baseFare) + " VND");
        if (tvTaxFeesTotal != null) tvTaxFeesTotal.setText(df.format(taxes) + " VND");
        if (tvSeatFeeValue != null) tvSeatFeeValue.setText(seatPrice > 0 ? df.format(seatPrice) + " VND" : "Miễn phí");
        if (tvAddonFeeValue != null) tvAddonFeeValue.setText(df.format(addonPrice) + " VND");
        if (tvGrandTotal != null) tvGrandTotal.setText(df.format(totalAmount) + " VND");

        view.findViewById(R.id.btnBack).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showExpiryPicker() {
        long today = com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds();
        com.google.android.material.datepicker.CalendarConstraints constraints =
                new com.google.android.material.datepicker.CalendarConstraints.Builder()
                .setStart(today)
                .setValidator(com.google.android.material.datepicker.DateValidatorPointForward.from(today))
                .build();

        com.google.android.material.datepicker.MaterialDatePicker<Long> picker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("CHỌN NGÀY HẾT HẠN")
                .setTheme(R.style.CustomDatePickerTheme)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(selection);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            String formattedMonth = String.format(java.util.Locale.getDefault(), "%02d", month);
            String formattedYear = String.valueOf(year).substring(2);
            binding.etExpiry.setText(formattedMonth + "/" + formattedYear);
        });
        picker.show(getSupportFragmentManager(), "ExpiryPicker");
    }

    private void processPayment() {
        if (selectedMethodName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Card Details if selected
        if ("card".equals(selectedMethodName)) {
            String cardNumber = binding.etCardNumber.getText().toString().replaceAll("\\s", "");
            String expiry = binding.etExpiry.getText().toString().trim();
            String cvv = binding.etCvv.getText().toString().trim();
            String holder = binding.etCardHolder.getText().toString().trim();

            if (cardNumber.length() < 16) {
                binding.etCardNumber.setError("Số thẻ phải đủ 16 chữ số");
                binding.etCardNumber.requestFocus();
                return;
            }
            if (expiry.isEmpty()) {
                binding.etExpiry.setError("Vui lòng chọn ngày hết hạn");
                return;
            }
            if (cvv.length() < 3) {
                binding.etCvv.setError("Mã CVV phải đủ 3 chữ số");
                binding.etCvv.requestFocus();
                return;
            }
            if (holder.isEmpty()) {
                binding.etCardHolder.setError("Vui lòng nhập tên chủ thẻ");
                binding.etCardHolder.requestFocus();
                return;
            }
        }

        binding.btnPay.setEnabled(false);
        binding.btnPay.setText("ĐANG XỬ LÝ GIAO DỊCH...");

        Intent intent;
        if ("card".equals(selectedMethodName)) {
            intent = new Intent(this, CardPaymentActivity.class);
        } else if ("vietqr".equals(selectedMethodName)) {
            intent = new Intent(this, VietQRPaymentActivity.class);
        } else if ("vnpay".equals(selectedMethodName)) {
            intent = new Intent(this, VNPayPaymentActivity.class);
        } else if ("momo".equals(selectedMethodName)) {
            intent = new Intent(this, MomoPaymentActivity.class);
        } else {
            intent = new Intent(this, PaymentProcessingActivity.class);
        }

        // Truyền dữ liệu sang màn hình thanh toán
        intent.putExtra("payment_method", selectedMethodName);
        intent.putExtra("totalAmount", totalAmount);
        intent.putExtra("passenger_name", passengerName);
        intent.putExtra("passenger_email", passengerEmail);
        intent.putExtra("selected_seat", selectedSeat);

        if ("card".equals(selectedMethodName)) {
            String cardNo = binding.etCardNumber.getText().toString().replaceAll("\\s", "");
            if (cardNo.length() >= 4) {
                intent.putExtra("card_masked", "**** **** **** " + cardNo.substring(cardNo.length() - 4));
            }
        }

        intent.putExtra("flight_json", getIntent().getStringExtra("flight_json"));
        intent.putExtra("return_flight_json", getIntent().getStringExtra("return_flight_json"));
        intent.putExtra("return_selected_seat", getIntent().getStringExtra("return_selected_seat"));

        startActivity(intent);
    }
}
