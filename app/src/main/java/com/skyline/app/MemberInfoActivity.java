package com.skyline.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    toast("Ứng dụng cần quyền Camera để chụp ảnh");
                }
            }
    );

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
        binding.tvMemberId.setText(user.getMemberCode());
        
        int points = user.getSkyPoints();
        String actualRank;
        int targetPoints;
        int badgeColor;

        // Tính toán hạng DỰA TRÊN ĐIỂM thực tế
        if (points < 1000) {
            actualRank = "ĐỒNG";
            targetPoints = 1000;
            badgeColor = 0xFF8D6E63;
        } else if (points < 5000) {
            actualRank = "BẠC";
            targetPoints = 5000;
            badgeColor = 0xFF455A64;
        } else {
            actualRank = "VÀNG";
            targetPoints = 5000;
            badgeColor = 0xFFBF953F;
        }

        binding.tvRankBadge.setText(actualRank);
        binding.tvMemberTier.setText("Hạng " + actualRank.toLowerCase());
        
        if ("VÀNG".equals(actualRank)) {
            binding.progressTier.setProgress(100);
            binding.tvSkyPoints.setText(String.valueOf(points));
        } else {
            binding.progressTier.setProgress(points * 100 / targetPoints);
            binding.tvSkyPoints.setText(points + " / " + targetPoints);
        }

        binding.tvRankBadge.setBackgroundTintList(ColorStateList.valueOf(badgeColor));
        binding.tvRankBadge.setTextColor(Color.WHITE);
        binding.tvMemberTier.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_medal, 0, 0, 0);
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(binding.tvMemberTier, ColorStateList.valueOf(badgeColor));

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
            if (isEditMode) showImagePickerBottomSheet();
            else showEditRequiredToast();
        });

        binding.btnEditInfo.setOnClickListener(v -> {
            if (isEditMode) {
                String newName = binding.edtUsername.getText().toString().trim();
                if (newName.isEmpty()) { toast("Vui lòng nhập họ tên"); return; }
                saveProfileChanges(newName);
            } else {
                binding.itemEmail.edtFieldValue.setText(binding.itemEmail.fieldValue.getText());
                binding.itemPhone.edtFieldValue.setText(binding.itemPhone.fieldValue.getText());
                binding.itemPassport.edtFieldValue.setText(binding.itemPassport.fieldValue.getText());
                isEditMode = true;
                updateEditModeUI();
            }
        });

        binding.btnBenefits.setOnClickListener(v -> startActivity(new Intent(this, RankDetailsActivity.class)));
        binding.btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private void showImagePickerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_image_picker, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            }
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_pick_gallery).setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_cancel_picker).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
