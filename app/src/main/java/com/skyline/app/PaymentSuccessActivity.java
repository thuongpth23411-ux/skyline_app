package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityPaymentSuccessBinding;
import com.skyline.model.Ticket;

public class PaymentSuccessActivity extends AppCompatActivity {
    private ActivityPaymentSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnViewTickets.setOnClickListener(v -> {
            // Get the flight information passed from PaymentOtpActivity
            String flightJson = getIntent().getStringExtra("flight_json");
            String selectedSeat = getIntent().getStringExtra("selected_seat");
            String passengerName = getIntent().getStringExtra("passenger_name");
            double totalAmount = getIntent().getDoubleExtra("totalAmount", 0);

            if (flightJson != null) {
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    com.skyline.app.network.Flight flight = gson.fromJson(flightJson, com.skyline.app.network.Flight.class);
                    
                    // Create a Ticket object to show details
                    java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("dd", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat monthYearFormat = new java.text.SimpleDateFormat("'THÁNG' MM\nyyyy", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault());
                    
                    String depTime = flight.getDepartureAt();
                    if (depTime == null) depTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()).format(new java.util.Date());
                    java.util.Date depDate = inputFormat.parse(depTime);
                    
                    Ticket ticket = new Ticket(
                        dayFormat.format(depDate),
                        monthYearFormat.format(depDate).toUpperCase(),
                        "Phổ thông",
                        "PENDING", // Actual code comes from server usually, but we show what we have
                        flight.getDepartureAirport().getCode(),
                        flight.getDepartureAirport().getCity(),
                        flight.getArrivalAirport().getCode(),
                        flight.getArrivalAirport().getCity(),
                        timeFormat.format(depDate),
                        selectedSeat != null ? selectedSeat : "--",
                        totalAmount,
                        passengerName != null ? passengerName : "Khách hàng",
                        "Chiều đi"
                    );

                    // Open Detail Fragment via HomeActivity
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("TARGET_FRAGMENT", "TICKET_DETAIL");
                    intent.putExtra("ticket_data", ticket);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể mở chi tiết vé ngay lúc này", Toast.LENGTH_SHORT).show();
                    // Fallback to flights list
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
                    startActivity(intent);
                    finish();
                }
            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
                startActivity(intent);
                finish();
            }
        });
    }
}
