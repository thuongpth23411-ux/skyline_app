package com.skyline.app;

import android.graphics.Paint;
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
    private double totalAmount, baseFare, addonPrice, seatPrice, taxes;
    private final Gson gson = new Gson();
    private String passengerEmail, passengerName, selectedSeat;
    private String selectedMethodName = "";
    private android.os.CountDownTimer paymentTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        initViews();
        startPaymentTimer();
    }

    private void startPaymentTimer() {
        paymentTimer = new android.os.CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                if (binding != null) {
                    binding.tvPaymentTimer.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (binding != null) {
                    binding.tvPaymentTimer.setText("00:00");
                }
                Toast.makeText(ConfirmPaymentActivity.this, "Hết thời gian thanh toán!", Toast.LENGTH_LONG).show();
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentTimer != null) paymentTimer.cancel();
    }

    private void initData() {
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("totalAmount", 0);
        baseFare = intent.getDoubleExtra("baseFare", 0);
        addonPrice = intent.getDoubleExtra("addonPrice", 0);
        seatPrice = intent.getDoubleExtra("seatPrice", 0);
        taxes = intent.getDoubleExtra("taxes", 0);

        passengerEmail = intent.getStringExtra("passenger_email");
        passengerName = intent.getStringExtra("passenger_name");
        selectedSeat = intent.getStringExtra("selected_seat");

        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvTotalPrice.setText(df.format(totalAmount) + " VND");
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.tvPriceDetail.setPaintFlags(binding.tvPriceDetail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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
        binding.etCardHolder.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.AllCaps(), new android.text.InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (!Character.isLetter(c) && c != ' ') {
                        return "";
                    }
                }
                return null;
            }
        }});
        
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
        TextView tvGrandTotal = view.findViewById(R.id.tvGrandTotal);
        if (tvGrandTotal != null) tvGrandTotal.setText(df.format(totalAmount) + " VND");

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showExpiryPicker() {
        long today = com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds();
        com.google.android.material.datepicker.CalendarConstraints constraints = 
                new com.google.android.material.datepicker.CalendarConstraints.Builder()
                .setValidator(com.google.android.material.datepicker.DateValidatorPointForward.from(today))
                .build();

        com.google.android.material.datepicker.MaterialDatePicker<Long> picker = 
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("CHỌN NGÀY HẾT HẠN")
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(selection);
            String formatted = String.format(Locale.getDefault(), "%02d/%d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR) % 100);
            binding.etExpiry.setText(formatted);
        });
        picker.show(getSupportFragmentManager(), "ExpiryPicker");
    }

    private void processPayment() {
        if (selectedMethodName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("card".equals(selectedMethodName)) {
            String cardNo = binding.etCardNumber.getText().toString().replaceAll("\\s", "");
            if (cardNo.length() < 16) {
                binding.etCardNumber.setError("Số thẻ phải đủ 16 chữ số");
                return;
            }
            if (binding.etExpiry.getText().toString().isEmpty()) {
                binding.etExpiry.setError("Vui lòng chọn ngày hết hạn");
                return;
            }
            if (binding.etCvv.getText().toString().length() < 3) {
                binding.etCvv.setError("Mã CVV phải đủ 3 chữ số");
                return;
            }
        }

        Intent intent;
        if ("card".equals(selectedMethodName)) intent = new Intent(this, CardPaymentActivity.class);
        else if ("vietqr".equals(selectedMethodName)) intent = new Intent(this, VietQRPaymentActivity.class);
        else if ("vnpay".equals(selectedMethodName)) intent = new Intent(this, VNPayPaymentActivity.class);
        else if ("momo".equals(selectedMethodName)) intent = new Intent(this, MomoPaymentActivity.class);
        else intent = new Intent(this, PaymentProcessingActivity.class);

        intent.putExtras(getIntent());
        intent.putExtra("payment_method", selectedMethodName);
        if ("card".equals(selectedMethodName)) {
            String cardNo = binding.etCardNumber.getText().toString().replaceAll("\\s", "");
            intent.putExtra("card_masked", "**** **** **** " + cardNo.substring(cardNo.length() - 4));
        }
        startActivity(intent);
    }
}
