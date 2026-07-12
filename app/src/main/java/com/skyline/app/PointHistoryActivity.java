package com.skyline.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.skyline.app.databinding.ActivityPointHistoryBinding;
import com.skyline.app.network.PointHistory;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PointHistoryActivity extends AppCompatActivity {

    private ActivityPointHistoryBinding binding;
    private PointHistoryAdapter adapter;
    private List<PointHistory> historyList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPointHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        setupRecyclerView();
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.swipeRefresh.setOnRefreshListener(this::fetchPointHistory);

        fetchPointHistory();
    }

    private void setupRecyclerView() {
        adapter = new PointHistoryAdapter(historyList);
        binding.rvPointHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPointHistory.setAdapter(adapter);
    }

    private void fetchPointHistory() {
        if (!sessionManager.isLoggedIn()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.fetchAuthToken();
        
        RetrofitClient.getInstance().getPointHistory(token).enqueue(new Callback<List<PointHistory>>() {
            @Override
            public void onResponse(@NonNull Call<List<PointHistory>> call, @NonNull Response<List<PointHistory>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    historyList = response.body();
                    adapter.updateData(historyList);
                    
                    if (historyList.isEmpty()) {
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                        binding.rvPointHistory.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmpty.setVisibility(View.GONE);
                        binding.rvPointHistory.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(PointHistoryActivity.this, "Không thể tải lịch sử tích điểm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PointHistory>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(PointHistoryActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
