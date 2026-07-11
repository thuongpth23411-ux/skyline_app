package com.skyline.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.skyline.app.databinding.ActivityPromotionsBinding;
import com.skyline.app.network.Promotion;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyVouchersActivity extends AppCompatActivity {
    private ActivityPromotionsBinding binding;
    private SessionManager sessionManager;
    private FullPromotionAdapter adapter;
    private Set<String> savedVoucherIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPromotionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        binding.tvHeaderTitle.setText("Voucher của tôi");
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.featuredCard.setVisibility(View.GONE);
        ((View)binding.chipGroup.getParent()).setVisibility(View.GONE);

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
        java.util.Map<String, String> body = new java.util.HashMap<>();
        String promoId = item.getId();
        body.put("promotionId", promoId);

        RetrofitClient.getInstance().toggleSaveVoucher(token, body).enqueue(new Callback<com.skyline.app.network.BaseResponse>() {
            @Override
            public void onResponse(Call<com.skyline.app.network.BaseResponse> call, Response<com.skyline.app.network.BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MyVouchersActivity.this, "Đã bỏ lưu voucher", Toast.LENGTH_SHORT).show();
                    loadMyVouchers(); // Refresh list to remove the item
                }
            }

            @Override
            public void onFailure(Call<com.skyline.app.network.BaseResponse> call, Throwable t) {
                Toast.makeText(MyVouchersActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyVouchers() {
        if (!sessionManager.isLoggedIn()) return;
        
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getMyVouchers(token).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Promotion> list = response.body();
                    savedVoucherIds.clear();
                    for (Promotion p : list) savedVoucherIds.add(p.getId());
                    adapter.setItems(list, savedVoucherIds);
                } else {
                    Toast.makeText(MyVouchersActivity.this, "Không có voucher nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Promotion>> call, Throwable t) {
                Toast.makeText(MyVouchersActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPromotionDetail(Promotion item) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_promotion_detail, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        TextView tvCode = view.findViewById(R.id.tv_code);
        TextView tvExpiry = view.findViewById(R.id.tv_expiry);
        ImageView imgPromo = view.findViewById(R.id.img_promo);
        
        tvTitle.setText(item.getTitle());
        
        // Làm đẹp nội dung mô tả: tự động xuống dòng sau mỗi dấu chấm
        String rawDesc = item.getDescription();
        String formattedDesc = (rawDesc != null) ? rawDesc.replace("\\n", "\n").replace(". ", ".\n\n") : "";
        tvDesc.setText(formattedDesc);

        tvCode.setText(item.getCode());
        tvExpiry.setText("Hạn dùng: " + item.getExpiryDate());

        int placeholderRes = R.drawable.img_brand_banner;
        String category = item.getCategory() != null ? item.getCategory() : "";
        if (category.contains("MEMBER")) placeholderRes = R.drawable.img_experience_first;
        else if (category.contains("EXCLUSIVE")) placeholderRes = R.drawable.bg_member_card_gradient;
        else if (category.contains("NEW_USER")) placeholderRes = R.drawable.img_experience_economy;
        
        String imageUrl = item.getImageUrl();
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
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
