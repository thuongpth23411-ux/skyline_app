package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityPaymentSuccessBinding;

public class PaymentSuccessActivity extends AppCompatActivity {

    private ActivityPaymentSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String bookingCode = getIntent().getStringExtra("bookingCode");
        String name = getIntent().getStringExtra("passenger_name");
        String method = getIntent().getStringExtra("payment_method");

        if (bookingCode != null) binding.tvBookingCode.setText(bookingCode);
        if (name != null) binding.tvPassengerName.setText(name);
        
        if (method != null) {
            String methodDisplay = method;
            if ("card".equals(method)) methodDisplay = "Thanh toán bằng thẻ";
            else if ("vnpay".equals(method)) methodDisplay = "Ví VNPay";
            else if ("vietqr".equals(method)) methodDisplay = "Chuyển khoản VietQR";
            else if ("momo".equals(method)) methodDisplay = "Ví MoMo";
            binding.tvPaymentMethod.setText(methodDisplay);
        }

        binding.btnViewTickets.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
