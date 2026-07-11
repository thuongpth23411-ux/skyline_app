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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromotionsActivity extends AppCompatActivity {
    private ActivityPromotionsBinding binding;
    private SessionManager sessionManager;
    private FullPromotionAdapter adapter;
    private List<Promotion> allPromotions = new ArrayList<>();
    private final Set<String> savedVoucherIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPromotionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        binding.btnBack.setOnClickListener(v -> finish());
        
        setupSearch();

        setupRecyclerView();
        loadPromotions();
        setupChips();
    }

    private void setupSearch() {
        binding.btnSearch.setOnClickListener(v -> toggleSearch(true));
        
        binding.btnActionSearch.setOnClickListener(v -> {
            String query = binding.edtHeaderSearch.getText().toString().toLowerCase().trim();
            performSearch(query);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.edtHeaderSearch.getWindowToken(), 0);
        });

        binding.btnCloseSearch.setOnClickListener(v -> {
            binding.edtHeaderSearch.setText("");
            toggleSearch(false);
            int checkedId = binding.chipGroup.getCheckedChipId();
            com.google.android.material.chip.Chip chip = findViewById(checkedId);
            if (chip != null) {
                filterPromotions(chip.getText().toString());
            } else {
                updateList(allPromotions);
            }
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
        }
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

    private void setupChips() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == -1) {
                binding.chipGroup.check(R.id.chip_all);
                return;
            }
            
            com.google.android.material.chip.Chip chip = findViewById(checkedId);
            if (chip != null) {
                String category = chip.getText().toString();
                filterPromotions(category);
                updateChipColors(group, checkedId);
            }
        });
    }

    private void updateChipColors(android.view.ViewGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if (view instanceof com.google.android.material.chip.Chip) {
                com.google.android.material.chip.Chip c = (com.google.android.material.chip.Chip) view;
                if (c.getId() == checkedId) {
                    c.setChipBackgroundColorResource(R.color.auth_blue_dark);
                    c.setTextColor(android.graphics.Color.WHITE);
                } else {
                    c.setChipBackgroundColorResource(R.color.profile_action_bg);
                    c.setTextColor(getResources().getColor(R.color.skyline_text_secondary));
                }
            }
        }
    }

    private void loadPromotions() {
        RetrofitClient.getInstance().getPromotions().enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Promotion>> call, @NonNull Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Promotion> rawPromotions = response.body();
                    
                    // Sắp xếp: Ưu tiên "Thứ 6 Mở App" lên đầu
                    List<Promotion> sorted = new ArrayList<>();
                    Promotion priorityItem = null;
                    for (Promotion p : rawPromotions) {
                        if (p.getTitle() != null && p.getTitle().contains("Thứ 6 Mở App")) {
                            priorityItem = p;
                            break;
                        }
                    }
                    if (priorityItem != null) {
                        sorted.add(priorityItem);
                    }
                    for (Promotion p : rawPromotions) {
                        if (p != priorityItem) {
                            sorted.add(p);
                        }
                    }
                    
                    allPromotions = sorted;
                    // Load thông tin voucher đã lưu để tô màu icon Bookmark
                    loadSavedVouchers();
                    checkOpenSpecificPromo();
                } else {
                    toast("Không có dữ liệu khuyến mãi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Promotion>> call, @NonNull Throwable t) {
                toast("Lỗi kết nối MongoDB: " + t.getMessage());
            }
        });
    }

    private void loadSavedVouchers() {
        if (!sessionManager.isLoggedIn()) {
            updateList(allPromotions);
            return;
        }

        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getMyVouchers(token).enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Promotion>> call, @NonNull Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedVoucherIds.clear();
                    for (Promotion p : response.body()) {
                        savedVoucherIds.add(p.getId());
                    }
                }
                updateList(allPromotions);
            }

            @Override
            public void onFailure(@NonNull Call<List<Promotion>> call, @NonNull Throwable t) {
                updateList(allPromotions);
            }
        });
    }

    private void filterPromotions(String category) {
        String query = binding.edtHeaderSearch.getText().toString().toLowerCase().trim();
        List<Promotion> filtered = new ArrayList<>();
        
        String dbCategory = getDbCategory(category);
        
        for (Promotion p : allPromotions) {
            boolean matchesCategory = dbCategory.isEmpty() || (p.getCategory() != null && p.getCategory().equalsIgnoreCase(dbCategory));
            boolean matchesQuery = query.isEmpty() || p.getTitle().toLowerCase().contains(query) || p.getCode().toLowerCase().contains(query);
            
            if (matchesCategory && matchesQuery) {
                filtered.add(p);
            }
        }
        updateList(filtered);
    }

    private String getDbCategory(String displayCategory) {
        if ("Thành viên".equalsIgnoreCase(displayCategory)) return "MEMBER";
        if ("Độc quyền".equalsIgnoreCase(displayCategory)) return "EXCLUSIVE";
        if ("Thanh toán".equalsIgnoreCase(displayCategory)) return "PAYMENT";
        if ("Khách hàng mới".equalsIgnoreCase(displayCategory)) return "NEW_USER";
        return "";
    }

    private void updateList(List<Promotion> list) {
        adapter.setItems(list, savedVoucherIds);
        
        // Kiểm tra xem có yêu cầu mở popup cụ thể không
        String targetPromoName = getIntent().getStringExtra("OPEN_PROMO_NAME");
        if (targetPromoName != null && !targetPromoName.isEmpty()) {
            for (Promotion p : list) {
                if (p.getTitle() != null && p.getTitle().contains(targetPromoName)) {
                    showPromotionDetail(p);
                    // Sau khi mở xong thì xóa intent extra để không bị mở lại khi quay lại activity
                    getIntent().removeExtra("OPEN_PROMO_NAME");
                    break;
                }
            }
        }
    }

    private void toggleSaveVoucher(Promotion item) {
        if (!sessionManager.isLoggedIn()) {
            toast("Vui lòng đăng nhập để lưu voucher");
            return;
        }

        String token = "Bearer " + sessionManager.fetchAuthToken();
        Map<String, String> body = new HashMap<>();
        String promoId = item.getId();
        body.put("promotionId", promoId);

        RetrofitClient.getInstance().toggleSaveVoucher(token, body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        if (savedVoucherIds.contains(promoId)) {
                            savedVoucherIds.remove(promoId);
                        } else {
                            savedVoucherIds.add(promoId);
                        }
                        adapter.notifyDataSetChanged();
                        toast(response.body().getMessage());
                    } else {
                        toast(response.body().getMessage());
                    }
                } else {
                    String errorMsg = "Lỗi xử lý";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JsonObject jsonObject = JsonParser.parseString(errorJson).getAsJsonObject();
                            if (jsonObject.has("message")) {
                                errorMsg = jsonObject.get("message").getAsString();
                            }
                        }
                    } catch (Exception e) {}
                    toast(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                toast("Lỗi kết nối máy chủ");
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
        
        // Làm đẹp nội dung mô tả: tự động xuống dòng sau mỗi dấu chấm
        String rawDesc = item.getDescription();
        String formattedDesc = (rawDesc != null) ? rawDesc.replace("\\n", "\n").replace(". ", ".\n\n") : "";
        tvDesc.setText(formattedDesc);

        tvCode.setText(item.getCode());
        tvExpiry.setText("Hạn dùng: " + item.getExpiryDate());

        // Nạp ảnh thật vào popup chi tiết
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
            copyToClipboard(item.getCode());
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void checkOpenSpecificPromo() {
        String promoId = getIntent().getStringExtra("OPEN_PROMO_ID");
        if (promoId != null && !promoId.isEmpty()) {
            for (Promotion p : allPromotions) {
                if (promoId.equals(p.getId())) {
                    showPromotionDetail(p);
                    break;
                }
            }
        }
    }

    private void performSearch(String query) {
        int checkedId = binding.chipGroup.getCheckedChipId();
        com.google.android.material.chip.Chip chip = findViewById(checkedId);
        String category = (chip != null) ? chip.getText().toString() : "Tất cả";
        filterPromotions(category);
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Promo Code", code);
        clipboard.setPrimaryClip(clip);
        toast("Đã sao chép mã: " + code);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
