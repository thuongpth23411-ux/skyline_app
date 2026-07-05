package com.skyline.app;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.skyline.app.databinding.ActivityConfirmPaymentBinding;
import com.skyline.app.databinding.DialogPriceDetailBinding;
import java.util.Calendar;
import java.util.Locale;

public class ConfirmPaymentActivity extends AppCompatActivity {

    private ActivityConfirmPaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHeader();
        setupPaymentMethods();
        setupCardTypeSpinner();
        setupExpiryDatePicker();
        setupTermsAndPay();
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Gạch chân Chi tiết giá
        binding.tvPriceDetail.setText(Html.fromHtml("<u>" + getString(R.string.price_detail) + "</u>", Html.FROM_HTML_MODE_COMPACT));
        
        binding.tvPriceDetail.setOnClickListener(v -> showPriceDetailDialog());
    }

    private void showPriceDetailDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        DialogPriceDetailBinding dialogBinding = DialogPriceDetailBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.btnBack.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupPaymentMethods() {
        binding.cardPaymentMethod.setOnClickListener(v -> selectPaymentMethod(0));
        binding.vnpayMethod.setOnClickListener(v -> selectPaymentMethod(1));
        binding.vietqrMethod.setOnClickListener(v -> selectPaymentMethod(2));
        binding.momoMethod.setOnClickListener(v -> selectPaymentMethod(3));

        binding.rbPaymentCard.setOnClickListener(v -> selectPaymentMethod(0));
        binding.rbVNPay.setOnClickListener(v -> selectPaymentMethod(1));
        binding.rbVietQR.setOnClickListener(v -> selectPaymentMethod(2));
        binding.rbMomo.setOnClickListener(v -> selectPaymentMethod(3));

        // Mặc định không chọn phương thức nào
        resetPaymentSelection();
    }

    private void resetPaymentSelection() {
        binding.rbPaymentCard.setChecked(false);
        binding.rbVNPay.setChecked(false);
        binding.rbVietQR.setChecked(false);
        binding.rbMomo.setChecked(false);

        binding.cardDetails.setVisibility(View.GONE);
        binding.tvVNPayRedirect.setVisibility(View.GONE);
        binding.tvVietQRRedirect.setVisibility(View.GONE);
        binding.tvMomoRedirect.setVisibility(View.GONE);

        int inactiveColor = Color.parseColor("#D1D5DB");
        binding.cardPaymentMethod.setStrokeColor(inactiveColor);
        binding.vnpayMethod.setStrokeColor(inactiveColor);
        binding.vietqrMethod.setStrokeColor(inactiveColor);
        binding.momoMethod.setStrokeColor(inactiveColor);
    }

    private void selectPaymentMethod(int index) {
        binding.rbPaymentCard.setChecked(index == 0);
        binding.rbVNPay.setChecked(index == 1);
        binding.rbVietQR.setChecked(index == 2);
        binding.rbMomo.setChecked(index == 3);

        binding.cardDetails.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        binding.tvVNPayRedirect.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        binding.tvVietQRRedirect.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        binding.tvMomoRedirect.setVisibility(index == 3 ? View.VISIBLE : View.GONE);

        // Đổi màu viền cho phương thức được chọn
        int activeColor = Color.BLACK;
        int inactiveColor = Color.parseColor("#D1D5DB");

        binding.cardPaymentMethod.setStrokeColor(index == 0 ? activeColor : inactiveColor);
        binding.vnpayMethod.setStrokeColor(index == 1 ? activeColor : inactiveColor);
        binding.vietqrMethod.setStrokeColor(index == 2 ? activeColor : inactiveColor);
        binding.momoMethod.setStrokeColor(index == 3 ? activeColor : inactiveColor);
    }

    private void setupCardTypeSpinner() {
        String[] cardTypes = {
            getString(R.string.visa),
            getString(R.string.mastercard),
            getString(R.string.jcb),
            getString(R.string.amex)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cardTypes);
        binding.spinnerCardType.setAdapter(adapter);
    }

    private void setupExpiryDatePicker() {
        binding.etExpiry.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                // Định dạng MM/YY
                String formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                String formattedYear = String.valueOf(selectedYear).substring(2);
                binding.etExpiry.setText(formattedMonth + "/" + formattedYear);
            }, year, month, day);

            // Chỉ chọn được các ngày tương lai
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupTermsAndPay() {
        // Hiển thị text với link màu xanh và gạch chân, phần còn lại màu đen
        String termsText = "<font color='#000000'>Tôi hiểu và đồng ý với </font>" +
                "<u><font color='#0B4DA2'>Điều lệ vận chuyển</font></u><font color='#000000'>, </font>" +
                "<u><font color='#0B4DA2'>Điều kiện điều khoản</font></u><font color='#000000'>, </font>" +
                "<u><font color='#0B4DA2'>Chính sách bảo mật</font></u><font color='#000000'> và </font>" +
                "<u><font color='#0B4DA2'>Điều kiện giá vé</font></u><font color='#000000'> của Skyline.</font>";

        binding.tvTerms.setText(Html.fromHtml(termsText, Html.FROM_HTML_MODE_COMPACT));

        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnPay.setEnabled(isChecked);
            binding.btnPay.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        binding.btnPay.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.processing_payment), Toast.LENGTH_LONG).show();
        });
    }
}
