package com.skyline.app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.ActivityFlightResultsBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSearchRequest;
import com.skyline.app.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightResultsActivity extends AppCompatActivity {

    private ActivityFlightResultsBinding binding;
    private FlightAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlightResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String fromCode = getIntent().getStringExtra("fromCode");
        String toCode = getIntent().getStringExtra("toCode");
        String date = getIntent().getStringExtra("date");

        binding.tvRoute.setText(fromCode + " \u2708 " + toCode);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnClose.setOnClickListener(v -> finish());

        setupRecyclerView();
        searchFlights(fromCode, toCode, date);
    }

    private void setupRecyclerView() {
        adapter = new FlightAdapter(new ArrayList<>(), new FlightAdapter.OnFlightClickListener() {
            @Override
            public void onFlightClick(Flight flight) {
                Toast.makeText(FlightResultsActivity.this, "Chọn chuyến bay: " + flight.getFlightNumber(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDetailClick(Flight flight) {
                // Show detail bottom sheet
            }
        });
        binding.rvFlights.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFlights.setAdapter(adapter);
    }

    private void searchFlights(String from, String to, String date) {
        FlightSearchRequest request = new FlightSearchRequest(from, to, date);
        RetrofitClient.getInstance().searchFlights(request).enqueue(new Callback<List<Flight>>() {
            @Override
            public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new FlightAdapter(response.body(), new FlightAdapter.OnFlightClickListener() {
                        @Override
                        public void onFlightClick(Flight flight) {
                            Toast.makeText(FlightResultsActivity.this, "Chọn: " + flight.getFlightNumber(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDetailClick(Flight flight) {
                            // Show detail
                        }
                    });
                    binding.rvFlights.setAdapter(adapter);
                } else {
                    Toast.makeText(FlightResultsActivity.this, "Không tìm thấy chuyến bay phù hợp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Flight>> call, Throwable t) {
                Toast.makeText(FlightResultsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
