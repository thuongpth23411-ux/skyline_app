package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.ActivityAirportSelectionBinding;
import com.skyline.app.network.Airport;
import com.skyline.app.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirportSelectionActivity extends AppCompatActivity {

    private ActivityAirportSelectionBinding binding;
    private AirportAdapter adapter;
    private List<Airport> allAirports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAirportSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String title = getIntent().getStringExtra("title");
        if (title != null) binding.tvTitle.setText(title);

        binding.btnClose.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadAirports();
        setupSearch();
    }

    private void setupRecyclerView() {
        boolean isFrom = getIntent().getBooleanExtra("isFrom", true);
        adapter = new AirportAdapter(new ArrayList<>(), airport -> {
            Intent intent = new Intent();
            intent.putExtra("code", airport.getCode());
            intent.putExtra("city", airport.getCity());
            intent.putExtra("name", airport.getName());
            intent.putExtra("isFrom", isFrom);
            setResult(RESULT_OK, intent);
            finish();
        });
        binding.rvAirports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAirports.setAdapter(adapter);
    }

    private void loadAirports() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance().getAirports().enqueue(new Callback<List<Airport>>() {
            @Override
            public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allAirports = response.body();
                    if (allAirports.isEmpty()) {
                        Toast.makeText(AirportSelectionActivity.this, "Danh sách sân bay trống", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.updateList(allAirports);
                    }
                } else {
                    Toast.makeText(AirportSelectionActivity.this, "Lỗi máy chủ: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Airport>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AirportSelectionActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        if (allAirports == null) return;
        List<Airport> filteredList = new ArrayList<>();
        for (Airport airport : allAirports) {
            String name = airport.getName() != null ? airport.getName().toLowerCase() : "";
            String city = airport.getCity() != null ? airport.getCity().toLowerCase() : "";
            String code = airport.getCode() != null ? airport.getCode().toLowerCase() : "";
            String search = text.toLowerCase();
            
            if (name.contains(search) || city.contains(search) || code.contains(search)) {
                filteredList.add(airport);
            }
        }
        adapter.updateList(filteredList);
    }
}
