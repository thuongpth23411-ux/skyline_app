package com.skyline.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.skyline.app.databinding.ActivityPromotionsBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.Promotion;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyVouchersActivity extends AppCompatActivity {
    private ActivityPromotionsBinding binding;
    private SessionManager sessionManager;
    private FullPromotionAdapter adapter;
    private List<Promotion> allVouchers = new ArrayList<>();
    private final Set<String> savedVoucherIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPromotionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        binding.tvHeaderTitle.setText("Voucher của tôi");
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Ẩn các phần không cần thiết của layout Promotions
        binding.featuredCard.setVisibility(View.GONE);
        if (binding.chipGroup.getParent() instanceof View) {
            ((View)binding.chipGroup.getParent()).setVisibility(View.GONE);
        }

        setupSearch();
        setupRecyclerView();
        loadMyVouchers();
    }

    private void setupSearch() {
        binding.btnSearch.setOnClickListener(v -> toggleSearch(true));
        
        binding.btnActionSearch.setOnClickListener(v -> {
            String query = binding.edtHeaderSearch.getText().toString().toLowerCase().trim();
            performSearch(query);
            hideKeyboard();
        });

        binding.btnCloseSearch.setOnClickListener(v -> {
            binding.edtHeaderSearch.setText("");
            toggleSearch(false);
            adapter.setItems(allVouchers, savedVoucherIds);
        });

        binding.edtHeaderSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().toLowerCase().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void toggleSearch(boolean show) {
        binding.layoutDefaultHeader.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.layoutSearchHeader.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            binding.edtHeaderSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(binding.edtHeaderSearch, InputMethodManager.SHOW_IMPLICIT);
        } else {
            hideKeyboard();
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            adapter.setItems(allVouchers, savedVoucherIds);
            return;
        }

        List<Promotion> filtered = new ArrayList<>();
        for (Promotion p : allVouchers) {
            String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
            String code = p.getCode() != null ? p.getCode().toLowerCase() : "";
            if (title.contains(query) || code.contains(query)) {
                filtered.add(p);
            }
        }
        adapter.setItems(filtered, savedVoucherIds);
    }

    private void setupRecyclerView() {
        adapter = new FullPromotionAdapter(new ArrayList<>(), new FullPromotionAdapter.OnPromotionClickListener() {
            @Override
            public void onCopyCode(String code) {
                copyToClipboard(code);
            }

            @Override
            public void onSaveVoucher(Promotion item) {
                toggleSaveVoucher(item);
            }

            @Override
            public void onItemClick(Promotion item) {
                showPromotionDetail(item);
            }
        });
        binding.rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPromotions.setAdapter(adapter);
    }

    private void toggleSaveVoucher(Promotion item) {
        if (!sessionManager.isLoggedIn()) return;

        String token = "Bearer " + sessionManager.fetchAuthToken();
        Map<String, String> body = new HashMap<>();
        String promoId = item.getId();
        body.put("promotionId", promoId);

        RetrofitClient.getInstance().toggleSaveVoucher(token, body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyVouchersActivity.this, "Đã bỏ lưu voucher", Toast.LENGTH_SHORT).show();
                    loadMyVouchers(); // Refresh list
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                toast("Lỗi kết nối");
            }
        });
    }

    private void loadMyVouchers() {
        if (!sessionManager.isLoggedIn()) return;
        
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getMyVouchers(token).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Promotion>> call, @NonNull Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allVouchers = response.body();
                    savedVoucherIds.clear();
                    for (Promotion p : allVouchers) savedVoucherIds.add(p.getId());
                    adapter.setItems(allVouchers, savedVoucherIds);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Promotion>> call, @NonNull Throwable t) {
                toast("Lỗi tải voucher");
            }
        });
    }

    private void showPromotionDetail(Promotion item) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_promotion_detail, null);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        TextView tvCode = view.findViewById(R.id.tv_code);
        TextView tvExpiry = view.findViewById(R.id.tv_expiry);
        ImageView imgPromo = view.findViewById(R.id.img_promo);
        
        tvTitle.setText(item.getTitle());
        String rawDesc = item.getDescription();
        String formattedDesc = (rawDesc != null) ? rawDesc.replace("\\n", "\n").replace(". ", ".\n\n") : "";
        tvDesc.setText(formattedDesc);

        tvCode.setText(item.getCode());
        tvExpiry.setText("Hạn dùng: " + item.getExpiryDate());

        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("/")) imageUrl = "http://10.0.2.2:3000" + imageUrl;
            com.bumptech.glide.Glide.with(this).load(imageUrl).into(imgPromo);
        }

        view.findViewById(R.id.btn_copy).setOnClickListener(v -> copyToClipboard(item.getCode()));
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Promo Code", code);
        clipboard.setPrimaryClip(clip);
        toast("Đã sao chép mã: " + code);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && binding.edtHeaderSearch.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(binding.edtHeaderSearch.getWindowToken(), 0);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
