package com.skyline.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityMemberInfoBinding;
import com.skyline.app.network.AuthResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.User;
import com.skyline.app.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberInfoActivity extends AppCompatActivity {

    private ActivityMemberInfoBinding binding;
    private SessionManager sessionManager;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        initLabelStatic();
        loadMemberData();
        setupClicks();
        updateEditModeUI();
    }

    private void initLabelStatic() {
        binding.itemEmail.fieldLabel.setText("EMAIL");
        binding.itemEmail.fieldValue.setText("...");
        binding.itemEmail.fieldIcon.setImageResource(R.drawable.ic_mail_auth);

        binding.itemDob.fieldLabel.setText("NGÀY SINH");
        binding.itemDob.fieldValue.setText("...");
        binding.itemDob.fieldIcon.setImageResource(R.drawable.ic_calendar_auth);

        binding.itemJoinDate.fieldLabel.setText("NGÀY THAM GIA");
        binding.itemJoinDate.fieldValue.setText("...");
        binding.itemJoinDate.fieldIcon.setImageResource(R.drawable.ic_calendar_check);

        binding.itemPhone.fieldLabel.setText("SỐ ĐIỆN THOẠI");
        binding.itemPhone.fieldValue.setText("...");
        binding.itemPhone.fieldIcon.setImageResource(R.drawable.ic_phone);

        binding.itemCccd.fieldLabel.setText("SỐ CCCD");
        binding.itemCccd.fieldValue.setText("...");
        binding.itemCccd.fieldIcon.setImageResource(R.drawable.ic_id_card);

        binding.itemPassport.fieldLabel.setText("HỘ CHIẾU");
        binding.itemPassport.fieldValue.setText("...");
        binding.itemPassport.fieldIcon.setImageResource(R.drawable.ic_check_auth);

        binding.itemCountry.fieldLabel.setText("QUỐC GIA");
        binding.itemCountry.fieldValue.setText("...");
        binding.itemCountry.fieldIcon.setImageResource(R.drawable.ic_location);

        binding.itemGender.fieldLabel.setText("GIỚI TÍNH");
        binding.itemGender.fieldValue.setText("...");
        binding.itemGender.fieldIcon.setImageResource(R.drawable.ic_gender);
    }

    private void loadMemberData() {
        if (!sessionManager.isLoggedIn()) return;

        binding.tvUsername.setText(sessionManager.getUserName());
        binding.tvMemberId.setText(sessionManager.getMemberCode());

        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    sessionManager.saveUser(user);
                    displayData(user);
                } else {
                    toast("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                toast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayData(User user) {
        if (user == null) return;
        
        binding.tvUsername.setText(user.getName() != null ? user.getName() : "Người dùng");
        binding.tvSkyPoints.setText(String.valueOf(user.getSkyPoints()));
        binding.tvMemberId.setText(user.getMemberCode() != null ? user.getMemberCode() : "---- ---- ----");
        
        String rank = user.getRank() != null ? user.getRank().toUpperCase() : "ĐỒNG";
        binding.tvRankBadge.setText(rank);
        binding.tvMemberTier.setText("Hạng " + rank.toLowerCase());
        
        if ("VÀNG".equals(rank)) binding.progressTier.setProgress(100);
        else if ("BẠC".equals(rank)) binding.progressTier.setProgress(60);
        else binding.progressTier.setProgress(0);

        binding.itemEmail.fieldValue.setText(user.getEmail());
        binding.itemEmail.imgLock.setVisibility(View.VISIBLE);

        binding.itemDob.fieldValue.setText(user.getDob() != null ? user.getDob() : "Chưa cập nhật");

        // Format Join Date (from MongoDB ISO string)
        String rawJoinDate = user.getJoinDate();
        String formattedDate = "--/--/----";
        if (rawJoinDate != null && rawJoinDate.length() >= 10) {
            try {
                // Lấy 10 ký tự đầu "YYYY-MM-DD"
                String dateOnly = rawJoinDate.substring(0, 10);
                String[] parts = dateOnly.split("-");
                formattedDate = String.format(Locale.getDefault(), "%s/%s/%s", parts[2], parts[1], parts[0]);
            } catch (Exception e) {
                formattedDate = rawJoinDate;
            }
        }
        binding.itemJoinDate.fieldValue.setText(formattedDate);
        binding.itemJoinDate.imgLock.setVisibility(View.VISIBLE);

        binding.itemPhone.fieldValue.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
        binding.itemPhone.imgLock.setVisibility(View.VISIBLE);

        binding.itemCccd.fieldValue.setText(user.getCccd() != null ? user.getCccd() : "Chưa cập nhật");
        binding.itemCccd.imgLock.setVisibility(View.VISIBLE);

        binding.itemPassport.fieldValue.setText(user.getPassport() != null ? user.getPassport() : "Chưa cập nhật");
        binding.itemCountry.fieldValue.setText(user.getCountry() != null ? user.getCountry() : "Chưa chọn");
        binding.itemCountry.imgEdit.setImageResource(R.drawable.ic_arrow_drop_down);
        
        // Mapping Title to Gender if Gender is null
        String gender = user.getGender();
        if (gender == null || gender.isEmpty()) {
            String title = user.getTitle();
            if ("Ông".equalsIgnoreCase(title)) gender = "Nam";
            else if ("Bà".equalsIgnoreCase(title)) gender = "Nữ";
            else gender = "Chưa chọn";
        }
        binding.itemGender.fieldValue.setText(gender);
        binding.itemGender.imgEdit.setImageResource(R.drawable.ic_arrow_drop_down);
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.itemDob.getRoot().setOnClickListener(v -> {
            if (isEditMode) showDatePicker();
            else showEditRequiredToast();
        });
        
        binding.itemPassport.getRoot().setOnClickListener(v -> {
            if (isEditMode) showPassportInputDialog();
            else showEditRequiredToast();
        });

        binding.itemCountry.getRoot().setOnClickListener(v -> {
            if (isEditMode) showCountryPicker();
            else showEditRequiredToast();
        });
        
        binding.itemGender.getRoot().setOnClickListener(v -> {
            if (isEditMode) showGenderPicker();
            else showEditRequiredToast();
        });
        
        binding.imgAvatar.setOnClickListener(v -> {
            if (isEditMode) toast("Chọn ảnh đại diện mới");
            else showEditRequiredToast();
        });

        View.OnClickListener lockedListener = v -> toast("Trường này không thể chỉnh sửa");
        binding.itemEmail.getRoot().setOnClickListener(lockedListener);
        binding.itemJoinDate.getRoot().setOnClickListener(lockedListener);
        binding.itemPhone.getRoot().setOnClickListener(lockedListener);
        binding.itemCccd.getRoot().setOnClickListener(lockedListener);

        binding.btnEditInfo.setOnClickListener(v -> {
            if (isEditMode) {
                // Save logic: sync from EditText to TextView
                binding.itemPassport.fieldValue.setText(binding.itemPassport.edtFieldValue.getText().toString());
                toast("Cập nhật thông tin thành công");
            } else {
                // Start edit: sync from TextView to EditText
                binding.itemPassport.edtFieldValue.setText(binding.itemPassport.fieldValue.getText().toString());
            }
            isEditMode = !isEditMode;
            updateEditModeUI();
        });

        binding.btnBenefits.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MemberInfoActivity.this, RankDetailsActivity.class);
            startActivity(intent);
        });
        
        binding.btnChangePassword.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MemberInfoActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void showCountryPicker() {
        String[] countries = {"Việt Nam", "Thái Lan", "Singapore", "Malaysia", "Hàn Quốc", "Nhật Bản", "Mỹ", "Pháp", "Đức", "Úc"};
        showListSelectorBottomSheet("Chọn quốc gia", countries, true, selection -> {
            binding.itemCountry.fieldValue.setText(selection);
        });
    }

    private void showGenderPicker() {
        String[] genders = {"Nam", "Nữ", "Khác"};
        showListSelectorBottomSheet("Chọn giới tính", genders, false, selection -> {
            binding.itemGender.fieldValue.setText(selection);
        });
    }

    private void showListSelectorBottomSheet(String title, String[] items, boolean showSearch, OnItemSelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_selector_country, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(title);

        View searchCard = view.findViewById(R.id.search_card);
        searchCard.setVisibility(showSearch ? View.VISIBLE : View.GONE);

        android.widget.ListView listView = view.findViewById(R.id.list_items);
        List<String> itemList = new ArrayList<>(Arrays.asList(items));
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this, R.layout.item_selector_row, R.id.tv_item_name, itemList);
        listView.setAdapter(adapter);

        if (showSearch) {
            androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return true;
                }
            });
        }

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            listener.onItemSelected(adapter.getItem(position));
            dialog.dismiss();
        });

        dialog.show();
    }

    interface OnItemSelectedListener {
        void onItemSelected(String selection);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày sinh")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar)
            .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            binding.itemDob.fieldValue.setText(sdf.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showPassportInputDialog() {
        android.widget.EditText editText = new android.widget.EditText(this);
        editText.setText(binding.itemPassport.fieldValue.getText());
        
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin/2, margin, margin/2);
        editText.setLayoutParams(params);
        container.addView(editText);

        new MaterialAlertDialogBuilder(this, R.style.SkylineDatePicker)
            .setTitle("Hộ chiếu")
            .setView(container)
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                binding.itemPassport.fieldValue.setText(editText.getText().toString());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void updateEditModeUI() {
        int visibility = isEditMode ? View.VISIBLE : View.GONE;
        binding.btnEditInfo.setText(isEditMode ? "Lưu thông tin" : "Chỉnh sửa thông tin");
        binding.btnEditInfo.setIconResource(isEditMode ? R.drawable.ic_check_auth : R.drawable.ic_edit);
        binding.btnChangePassword.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        
        binding.itemDob.imgEdit.setVisibility(visibility);
        binding.itemCountry.imgEdit.setVisibility(visibility);
        binding.itemGender.imgEdit.setVisibility(visibility);

        // Passport in-place edit logic
        binding.itemPassport.fieldValue.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        binding.itemPassport.edtFieldValue.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        if (isEditMode) {
            binding.itemPassport.edtFieldValue.requestFocus();
        }
    }

    private void showEditRequiredToast() {
        toast("Vui lòng nhấn 'Chỉnh sửa thông tin' để thay đổi");
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
