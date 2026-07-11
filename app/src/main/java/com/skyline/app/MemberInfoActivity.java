package com.skyline.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.skyline.app.databinding.ActivityMemberInfoBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.User;
import com.skyline.app.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberInfoActivity extends AppCompatActivity {

    private ActivityMemberInfoBinding binding;
    private SessionManager sessionManager;
    private boolean isEditMode = false;
    private Uri cameraImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    binding.imgAvatar.setImageURI(uri);
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    binding.imgAvatar.setImageURI(cameraImageUri);
                }
            }
    );

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
        binding.itemEmail.fieldIcon.setImageResource(R.drawable.ic_mail_auth);

        binding.itemDob.fieldLabel.setText("NGÀY SINH");
        binding.itemDob.fieldIcon.setImageResource(R.drawable.ic_calendar_auth);

        binding.itemJoinDate.fieldLabel.setText("NGÀY THAM GIA");
        binding.itemJoinDate.fieldIcon.setImageResource(R.drawable.ic_calendar_check);

        binding.itemPhone.fieldLabel.setText("SỐ ĐIỆN THOẠI");
        binding.itemPhone.fieldIcon.setImageResource(R.drawable.ic_phone);

        binding.itemCccd.fieldLabel.setText("SỐ CCCD");
        binding.itemCccd.fieldIcon.setImageResource(R.drawable.ic_id_card);

        binding.itemPassport.fieldLabel.setText("HỘ CHIẾU");
        binding.itemPassport.fieldIcon.setImageResource(R.drawable.ic_check_auth);

        binding.itemCountry.fieldLabel.setText("QUỐC GIA");
        binding.itemCountry.fieldIcon.setImageResource(R.drawable.ic_location);

        binding.itemGender.fieldLabel.setText("GIỚI TÍNH");
        binding.itemGender.fieldIcon.setImageResource(R.drawable.ic_gender);
    }

    private void loadMemberData() {
        if (!sessionManager.isLoggedIn()) return;

        binding.tvUsername.setText(sessionManager.getUserName());
        binding.edtUsername.setText(sessionManager.getUserName());
        binding.tvMemberId.setText(sessionManager.getMemberCode());

        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    sessionManager.saveUser(user);
                    displayData(user);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                toast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayData(User user) {
        if (user == null) return;
        
        binding.tvUsername.setText(user.getName());
        binding.edtUsername.setText(user.getName());
        binding.tvSkyPoints.setText(String.valueOf(user.getSkyPoints()));
        binding.tvMemberId.setText(user.getMemberCode());
        
        String rank = user.getRank() != null ? user.getRank().toUpperCase() : "ĐỒNG";
        binding.tvRankBadge.setText(rank);
        binding.tvMemberTier.setText("Hạng " + rank.toLowerCase());
        
        if ("VÀNG".equals(rank)) binding.progressTier.setProgress(100);
        else if ("BẠC".equals(rank)) binding.progressTier.setProgress(60);
        else binding.progressTier.setProgress(0);

        binding.itemEmail.fieldValue.setText(user.getEmail());
        binding.itemEmail.imgLock.setVisibility(View.GONE);

        binding.itemDob.fieldValue.setText(user.getDob() != null ? user.getDob() : "Chưa cập nhật");
        binding.itemDob.imgLock.setVisibility(View.GONE);

        String rawJoinDate = user.getJoinDate();
        String formattedDate = "--/--/----";
        if (rawJoinDate != null && rawJoinDate.length() >= 10) {
            try {
                String dateOnly = rawJoinDate.contains("T") ? rawJoinDate.split("T")[0] : rawJoinDate.substring(0, 10);
                String[] parts = dateOnly.split("-");
                if (parts.length == 3) formattedDate = String.format("%s/%s/%s", parts[2], parts[1], parts[0]);
            } catch (Exception ignored) {}
        }
        binding.itemJoinDate.fieldValue.setText(formattedDate);
        binding.itemJoinDate.imgLock.setVisibility(View.VISIBLE);

        binding.itemPhone.fieldValue.setText(user.getPhone());
        binding.itemPhone.imgLock.setVisibility(View.GONE);

        binding.itemCccd.fieldValue.setText(user.getCccd());
        binding.itemCccd.imgLock.setVisibility(View.VISIBLE);

        binding.itemPassport.fieldValue.setText(user.getPassport());
        binding.itemPassport.imgLock.setVisibility(View.GONE);
        binding.itemCountry.fieldValue.setText(user.getCountry() != null ? user.getCountry() : "Chưa chọn");
        binding.itemCountry.imgEdit.setImageResource(R.drawable.ic_arrow_drop_down);
        
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

        binding.itemEmail.getRoot().setOnClickListener(v -> {
            if (isEditMode) binding.itemEmail.edtFieldValue.requestFocus();
            else showEditRequiredToast();
        });

        binding.itemDob.getRoot().setOnClickListener(v -> {
            if (isEditMode) showDatePicker();
            else showEditRequiredToast();
        });

        binding.itemPhone.getRoot().setOnClickListener(v -> {
            if (isEditMode) binding.itemPhone.edtFieldValue.requestFocus();
            else showEditRequiredToast();
        });

        binding.itemPassport.getRoot().setOnClickListener(v -> {
            if (isEditMode) binding.itemPassport.edtFieldValue.requestFocus();
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
            if (isEditMode) showImageSourceDialog();
            else showEditRequiredToast();
        });

        binding.btnEditInfo.setOnClickListener(v -> {
            if (isEditMode) {
                String newName = binding.edtUsername.getText().toString().trim();
                if (newName.isEmpty()) { toast("Vui lòng nhập họ tên"); return; }
                saveProfileChanges(newName);
            } else {
                // Sync values to EditTexts before entering edit mode
                binding.itemEmail.edtFieldValue.setText(binding.itemEmail.fieldValue.getText());
                binding.itemPhone.edtFieldValue.setText(binding.itemPhone.fieldValue.getText());
                binding.itemPassport.edtFieldValue.setText(binding.itemPassport.fieldValue.getText());

                isEditMode = true;
                updateEditModeUI();
            }
        });

        binding.btnBenefits.setOnClickListener(v -> startActivity(new Intent(this, RankDetailsActivity.class)));
        binding.btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cập nhật ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else pickImageLauncher.launch("image/*");
                })
                .show();
    }

    private void openCamera() {
        File photoFile = new File(getExternalCacheDir(), "avatar_temp.jpg");
        cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        takePhotoLauncher.launch(cameraImageUri);
    }

    private void saveProfileChanges(String name) {
        String token = "Bearer " + sessionManager.fetchAuthToken();
        Map<String, String> body = new HashMap<>();
        body.put("fullName", name);
        body.put("email", binding.itemEmail.edtFieldValue.getText().toString().trim());
        body.put("phone", binding.itemPhone.edtFieldValue.getText().toString().trim());
        body.put("passport", binding.itemPassport.edtFieldValue.getText().toString().trim());
        body.put("dob", binding.itemDob.fieldValue.getText().toString());
        body.put("country", binding.itemCountry.fieldValue.getText().toString());
        body.put("gender", binding.itemGender.fieldValue.getText().toString());

        RetrofitClient.getInstance().updateProfile(token, body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    toast("Cập nhật thông tin thành công");
                    isEditMode = false;
                    updateEditModeUI();
                    loadMemberData();
                } else {
                    toast("Cập nhật thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                toast("Lỗi kết nối");
            }
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

        ((TextView)view.findViewById(R.id.tv_title)).setText(title);
        view.findViewById(R.id.search_card).setVisibility(showSearch ? View.VISIBLE : View.GONE);

        android.widget.ListView listView = view.findViewById(R.id.list_items);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, R.layout.item_selector_row, R.id.tv_item_name, new ArrayList<>(Arrays.asList(items)));
        listView.setAdapter(adapter);

        if (showSearch) {
            ((androidx.appcompat.widget.SearchView)view.findViewById(R.id.search_view)).setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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

    interface OnItemSelectedListener { void onItemSelected(String selection); }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày sinh")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTheme(R.style.CustomDatePickerTheme)
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
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(-1, -2);
        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin/2, margin, margin/2);
        editText.setLayoutParams(params);
        container.addView(editText);

        new MaterialAlertDialogBuilder(this, R.style.SkylineDatePicker)
            .setTitle("Hộ chiếu").setView(container)
            .setPositiveButton("Xác nhận", (dialog, which) -> binding.itemPassport.fieldValue.setText(editText.getText().toString()))
            .setNegativeButton("Hủy", null).show();
    }

    private void updateEditModeUI() {
        int visibility = isEditMode ? View.VISIBLE : View.GONE;
        binding.btnEditInfo.setText(isEditMode ? "Lưu thông tin" : "Chỉnh sửa thông tin");
        binding.btnEditInfo.setIconResource(isEditMode ? R.drawable.ic_check_auth : R.drawable.ic_edit);
        binding.btnChangePassword.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        binding.tvUsername.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        binding.edtUsername.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.itemDob.imgEdit.setVisibility(visibility);
        binding.itemCountry.imgEdit.setVisibility(visibility);
        binding.itemGender.imgEdit.setVisibility(visibility);

        binding.itemEmail.fieldValue.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        binding.itemEmail.edtFieldValue.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        
        binding.itemPhone.fieldValue.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        binding.itemPhone.edtFieldValue.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        binding.itemPassport.fieldValue.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        binding.itemPassport.edtFieldValue.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        if (isEditMode) binding.edtUsername.requestFocus();
    }

    private void showEditRequiredToast() { toast("Vui lòng nhấn 'Chỉnh sửa thông tin' để thay đổi"); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
