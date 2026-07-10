package com.skyline.app;

import android.graphics.Paint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityConfirmPaymentBinding;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ConfirmPaymentActivity extends AppCompatActivity {

    private ActivityConfirmPaymentBinding binding;
    private double totalAmount, baseFare, addonPrice, seatPrice, taxes;
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
        String totalStr = df.format(totalAmount) + " VND";
        binding.tvTotalPrice.setText(totalStr);
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Gạch chân Chi tiết giá và đảm bảo click được
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
        
        // Không chọn mặc định phương thức nào
        clearPaymentSelections();
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

        // Highlight selected - Viền đen (không màu)
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
        if (selectedMethodName.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPay.setEnabled(false);
        binding.btnPay.setText("ĐANG CHUYỂN HƯỚNG...");

        Intent intent;
        if ("card".equals(selectedMethodName)) {
            intent = new Intent(this, CardPaymentActivity.class);
        } else if ("vietqr".equals(selectedMethodName)) {
            intent = new Intent(this, VietQRPaymentActivity.class);
        } else {
            intent = new Intent(this, PaymentProcessingActivity.class);
        }

        // Truyền dữ liệu sang màn hình thanh toán
        intent.putExtra("payment_method", selectedMethodName);
        intent.putExtra("totalAmount", totalAmount);
        intent.putExtra("passenger_name", passengerName);
        intent.putExtra("passenger_email", passengerEmail);
        intent.putExtra("selected_seat", selectedSeat);
        intent.putExtra("flight_json", getIntent().getStringExtra("flight_json"));
        intent.putExtra("return_flight_json", getIntent().getStringExtra("return_flight_json"));
        intent.putExtra("return_selected_seat", getIntent().getStringExtra("return_selected_seat"));

        startActivity(intent);
    }
}
