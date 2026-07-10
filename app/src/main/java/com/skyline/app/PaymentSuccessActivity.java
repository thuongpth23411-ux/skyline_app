package com.skyline.app;

import com.skyline.app.utils.NotificationHelper;
import com.skyline.app.utils.SessionManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityPaymentSuccessBinding;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;

public class PaymentSuccessActivity extends AppCompatActivity {

    private ActivityPaymentSuccessBinding binding;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        displayData();
        triggerNotification();

        binding.btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void displayData() {
        Intent intent = getIntent();
        double total = intent.getDoubleExtra("totalAmount", 0);
        String name = intent.getStringExtra("passenger_name");
        String seat = intent.getStringExtra("selected_seat");
        String json = intent.getStringExtra("flight_json");

        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvTotalPaid.setText(df.format(total) + " VND");
        binding.tvPassengerName.setText(name != null ? name.toUpperCase() : "HÀNH KHÁCH");
        binding.tvSeatNumber.setText(seat != null ? seat : "Chưa chọn");
        
        binding.tvTransactionId.setText("Mã giao dịch: #SKL" + (System.currentTimeMillis() % 10000000));

        if (json != null) {
            Flight flight = gson.fromJson(json, Flight.class);
            if (flight != null) {
                if (flight.getDepartureAirport() != null) {
                    binding.tvFromCode.setText(flight.getDepartureAirport().getCode());
                    binding.tvFromCity.setText(flight.getDepartureAirport().getCity());
                }
                if (flight.getArrivalAirport() != null) {
                    binding.tvToCode.setText(flight.getArrivalAirport().getCode());
                    binding.tvToCity.setText(flight.getArrivalAirport().getCity());
                }
                binding.tvFlightNumber.setText(flight.getFlightNumber());
            }
        }
        
        String returnJson = intent.getStringExtra("return_flight_json");
        if (returnJson != null) {
            // Hiển thị thêm thông tin khứ hồi nếu cần, hoặc đơn giản là thông báo đã lưu 2 vé
            binding.tvTransactionId.append("\n(Đã lưu vé Chiều đi & Chiều về)");
        }
    }

    private void triggerNotification() {
        NotificationHelper.showDropDownNotification(
            this,
            "BOOKING_SUCCESS_" + System.currentTimeMillis(),
            "Đặt vé thành công",
            "Chúc mừng! Bạn đã đặt vé thành công. Nhấn để xem lịch trình.",
            NotificationHelper.NotifType.TICKET,
            null
        );
    }
}
