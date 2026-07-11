package com.skyline.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private List<Promotion> currentVouchers = new ArrayList<>();
    private final Set<String> savedVoucherIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPromotionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        binding.tvHeaderTitle.setText("Voucher của tôi");
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.featuredCard.setVisibility(View.GONE);
        if (binding.chipGroup.getParent() instanceof View) {
            ((View)binding.chipGroup.getParent()).setVisibility(View.GONE);
        }

        setupRecyclerView();
        loadMyVouchers();
    }

    private void setupRecyclerView() {
        adapter = new FullPromotionAdapter(new ArrayList<>(), new FullPromotionAdapter.OnPromotionClickListener() {
            @Override
            public void onCopyCode(String code) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Promo Code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MyVouchersActivity.this, "Đã sao chép mã: " + code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSaveVoucher(Promotion item) {
                toggleUnsaveVoucher(item);
            }

            @Override
            public void onItemClick(Promotion item) {
                showPromotionDetail(item);
            }
        });
        binding.rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPromotions.setAdapter(adapter);
    }

    private void loadMyVouchers() {
        if (!sessionManager.isLoggedIn()) return;
        
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getMyVouchers(token).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Promotion>> call, @NonNull Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentVouchers = response.body();
                    savedVoucherIds.clear();
                    for (Promotion p : currentVouchers) {
                        if (p.getId() != null) savedVoucherIds.add(p.getId());
                    }
                    adapter.setItems(currentVouchers, savedVoucherIds);
                } else {
                    Toast.makeText(MyVouchersActivity.this, "Không có voucher nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Promotion>> call, @NonNull Throwable t) {
                Toast.makeText(MyVouchersActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleUnsaveVoucher(Promotion item) {
        String token = "Bearer " + sessionManager.fetchAuthToken();
        Map<String, String> body = new HashMap<>();
        String promoId = item.getId();
        body.put("promotionId", promoId);

        RetrofitClient.getInstance().toggleSaveVoucher(token, body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Xóa voucher khỏi danh sách hiển thị
                    List<Promotion> newList = new ArrayList<>();
                    for (Promotion p : currentVouchers) {
                        if (p.getId() != null && !p.getId().equals(promoId)) {
                            newList.add(p);
                        }
                    }
                    currentVouchers = newList;
                    savedVoucherIds.remove(promoId);
                    adapter.setItems(currentVouchers, savedVoucherIds);
                    Toast.makeText(MyVouchersActivity.this, "Đã bỏ lưu voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(MyVouchersActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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
        int placeholderRes = R.drawable.img_brand_banner;
        String category = item.getCategory();
        if (category.contains("MEMBER")) placeholderRes = R.drawable.img_experience_first;
        else if (category.contains("EXCLUSIVE")) placeholderRes = R.drawable.bg_member_card_gradient;
        else if (category.contains("NEW_USER")) placeholderRes = R.drawable.img_experience_economy;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("/")) imageUrl = "http://10.0.2.2:3000" + imageUrl;
            com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .into(imgPromo);
        } else {
            imgPromo.setImageResource(placeholderRes);
        }

        view.findViewById(R.id.btn_copy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Promo Code", item.getCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép mã", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
