package com.skyline.app;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.skyline.app.databinding.ActivityPassengerDirectoryBinding;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.PassengerDirectory;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.User;
import com.skyline.app.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerDirectoryActivity extends AppCompatActivity {
    private ActivityPassengerDirectoryBinding binding;
    private SessionManager sessionManager;
    private User currentUser;
    private PassengerDirectoryAdapter adapter;
    private List<PassengerDirectory> passengerList = new ArrayList<>();
    private List<PassengerDirectory> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPassengerDirectoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        setupUI();
        loadLatestUserInfo();
        loadMyPassengers();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.headerMembership.setOnClickListener(v -> {
            boolean isVisible = binding.cardMemberUser.getVisibility() == View.VISIBLE;
            binding.cardMemberUser.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            binding.imgExpandMembership.setRotation(isVisible ? 180 : 0);
        });

        binding.cardMemberUser.setOnClickListener(v -> {
            if (currentUser != null) showUserDetail(currentUser);
        });

        binding.btnAddHeader.setOnClickListener(v -> showAddPassengerDialog());

        adapter = new PassengerDirectoryAdapter(filteredList, this::showPassengerDetail);
        binding.rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPassengers.setAdapter(adapter);

        binding.edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(passengerList);
        } else {
            String query = text.toLowerCase().trim();
            for (PassengerDirectory item : passengerList) {
                if (item.getPassengerName().toLowerCase().contains(query) || 
                    (item.getPassengerPhone() != null && item.getPassengerPhone().contains(query))) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadLatestUserInfo() {
        if (!sessionManager.isLoggedIn()) return;
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayUserInfo(currentUser);
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
        });
    }

    private void loadMyPassengers() {
        if (!sessionManager.isLoggedIn()) return;
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getMyPassengers(token).enqueue(new Callback<List<PassengerDirectory>>() {
            @Override
            public void onResponse(@NonNull Call<List<PassengerDirectory>> call, @NonNull Response<List<PassengerDirectory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    passengerList = response.body();
                    filter(binding.edtSearch.getText().toString()); // Cập nhật danh sách hiển thị
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<PassengerDirectory>> call, @NonNull Throwable t) {
                toast("Lỗi tải danh bạ");
            }
        });
    }

    private void displayUserInfo(User user) {
        String title = "Nam".equalsIgnoreCase(user.getGender()) ? "(ÔNG) " : "Nữ".equalsIgnoreCase(user.getGender()) ? "(BÀ) " : "";
        binding.tvMemberName.setText(String.format("%s%s", title, user.getName() != null ? user.getName().toUpperCase() : ""));
        binding.tvMemberCode.setText(user.getMemberCode());
        binding.tvMemberInitials.setText(getInitials(user.getName()));
    }

    private void showUserDetail(User user) {
        Dialog dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_passenger_detail, null);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View layoutView = view.findViewById(R.id.layout_view_mode);
        View layoutEdit = view.findViewById(R.id.layout_edit_mode);
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        ImageView btnEditMode = view.findViewById(R.id.btn_edit_mode);
        Button btnClose = view.findViewById(R.id.btn_close_detail);

        TextView tvViewName = view.findViewById(R.id.tv_view_name);
        TextView tvViewPhone = view.findViewById(R.id.tv_view_phone);
        TextView tvViewId = view.findViewById(R.id.tv_view_id);
        TextView tvViewDob = view.findViewById(R.id.tv_view_dob);
        TextView tvViewEmail = view.findViewById(R.id.tv_view_email);

        EditText edtName = view.findViewById(R.id.edt_edit_name);
        EditText edtPhone = view.findViewById(R.id.edt_edit_phone);
        EditText edtId = view.findViewById(R.id.edt_edit_id);
        EditText edtDob = view.findViewById(R.id.edt_edit_dob);
        EditText edtEmail = view.findViewById(R.id.edt_edit_email);

        String title = "Nam".equalsIgnoreCase(user.getGender()) ? "(Ông) " : "Nữ".equalsIgnoreCase(user.getGender()) ? "(Bà) " : "";
        tvViewName.setText(String.format("%s%s", title, user.getName() != null ? user.getName().toUpperCase() : ""));
        tvViewPhone.setText(user.getPhone());
        tvViewId.setText(user.getCccd() != null ? user.getCccd() : user.getPassport());
        tvViewDob.setText(user.getDob());
        tvViewEmail.setText(user.getEmail());

        btnEditMode.setOnClickListener(v -> {
            layoutView.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.VISIBLE);
            btnEditMode.setVisibility(View.GONE);
            tvTitle.setText("Chỉnh sửa thông tin");
            btnClose.setText("Hủy");
            edtName.setText(user.getName());
            edtPhone.setText(user.getPhone());
            edtId.setText(user.getCccd() != null ? user.getCccd() : user.getPassport());
            edtDob.setText(user.getDob());
            edtEmail.setText(user.getEmail());
        });

        edtDob.setOnClickListener(v -> showDatePicker(edtDob));

        view.findViewById(R.id.btn_save_changes).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) { toast("Vui lòng nhập họ tên"); return; }

            Map<String, String> body = new HashMap<>();
            body.put("fullName", name);
            body.put("phone", edtPhone.getText().toString().trim());
            body.put("cccd", edtId.getText().toString().trim());
            body.put("dob", edtDob.getText().toString().trim());
            body.put("email", edtEmail.getText().toString().trim());

            String token = "Bearer " + sessionManager.fetchAuthToken();
            RetrofitClient.getInstance().updateProfile(token, body).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        toast("Cập nhật thành công");
                        loadLatestUserInfo();
                        dialog.dismiss();
                    } else {
                        toast("Lỗi cập nhật hồ sơ");
                    }
                }
                @Override
                public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) { toast("Lỗi kết nối"); }
            });
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPassengerDetail(PassengerDirectory passenger) {
        Dialog dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_passenger_detail, null);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View layoutView = view.findViewById(R.id.layout_view_mode);
        View layoutEdit = view.findViewById(R.id.layout_edit_mode);
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        ImageView btnEditMode = view.findViewById(R.id.btn_edit_mode);
        Button btnClose = view.findViewById(R.id.btn_close_detail);

        TextView tvViewName = view.findViewById(R.id.tv_view_name);
        TextView tvViewPhone = view.findViewById(R.id.tv_view_phone);
        TextView tvViewId = view.findViewById(R.id.tv_view_id);
        TextView tvViewDob = view.findViewById(R.id.tv_view_dob);
        TextView tvViewEmail = view.findViewById(R.id.tv_view_email);

        EditText edtName = view.findViewById(R.id.edt_edit_name);
        EditText edtPhone = view.findViewById(R.id.edt_edit_phone);
        EditText edtId = view.findViewById(R.id.edt_edit_id);
        EditText edtDob = view.findViewById(R.id.edt_edit_dob);
        EditText edtEmail = view.findViewById(R.id.edt_edit_email);

        tvViewName.setText(passenger.getPassengerName().toUpperCase());
        tvViewPhone.setText(passenger.getPassengerPhone());
        tvViewId.setText(passenger.getPassengerCccd());
        tvViewDob.setText(passenger.getPassengerDob());
        tvViewEmail.setText(passenger.getPassengerEmail());

        btnEditMode.setOnClickListener(v -> {
            layoutView.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.VISIBLE);
            btnEditMode.setVisibility(View.GONE);
            tvTitle.setText("Sửa hành khách");
            btnClose.setText("Hủy");
            edtName.setText(passenger.getPassengerName());
            edtPhone.setText(passenger.getPassengerPhone());
            edtId.setText(passenger.getPassengerCccd());
            edtDob.setText(passenger.getPassengerDob());
            edtEmail.setText(passenger.getPassengerEmail());
        });

        edtDob.setOnClickListener(v -> showDatePicker(edtDob));

        view.findViewById(R.id.btn_save_changes).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) { toast("Vui lòng nhập họ tên"); return; }

            Map<String, String> body = new HashMap<>();
            body.put("passengerName", name);
            body.put("passengerPhone", edtPhone.getText().toString().trim());
            body.put("passengerCccd", edtId.getText().toString().trim());
            body.put("passengerDob", edtDob.getText().toString().trim());
            body.put("passengerEmail", edtEmail.getText().toString().trim());

            String token = "Bearer " + sessionManager.fetchAuthToken();
            RetrofitClient.getInstance().updatePassenger(token, passenger.getId(), body).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        toast("Cập nhật thành công");
                        loadMyPassengers();
                        dialog.dismiss();
                    } else {
                        toast("Lỗi cập nhật hành khách");
                    }
                }
                @Override
                public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) { toast("Lỗi kết nối"); }
            });
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddPassengerDialog() {
        Dialog dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_passenger_detail, null);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        view.findViewById(R.id.layout_view_mode).setVisibility(View.GONE);
        view.findViewById(R.id.layout_edit_mode).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btn_edit_mode).setVisibility(View.GONE);
        
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        tvTitle.setText("Thêm hành khách mới");
        
        Button btnSave = view.findViewById(R.id.btn_save_changes);
        btnSave.setText("Thêm ngay");
        
        EditText edtName = view.findViewById(R.id.edt_edit_name);
        EditText edtPhone = view.findViewById(R.id.edt_edit_phone);
        EditText edtId = view.findViewById(R.id.edt_edit_id);
        EditText edtDob = view.findViewById(R.id.edt_edit_dob);
        EditText edtEmail = view.findViewById(R.id.edt_edit_email);

        edtDob.setOnClickListener(v -> showDatePicker(edtDob));

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                toast("Vui lòng nhập họ tên");
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("passengerName", name);
            body.put("passengerPhone", edtPhone.getText().toString().trim());
            body.put("passengerCccd", edtId.getText().toString().trim());
            body.put("passengerDob", edtDob.getText().toString().trim());
            body.put("passengerEmail", edtEmail.getText().toString().trim());

            String token = "Bearer " + sessionManager.fetchAuthToken();
            RetrofitClient.getInstance().addPassenger(token, body).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        toast("Thêm hành khách thành công");
                        loadMyPassengers();
                        dialog.dismiss();
                    } else {
                        String errorMsg = "Lỗi xử lý";
                        try {
                            if (response.errorBody() != null) {
                                String errorJson = response.errorBody().string();
                                com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(errorJson).getAsJsonObject();
                                if (jsonObject.has("message")) {
                                    errorMsg = jsonObject.get("message").getAsString();
                                }
                            } else if (response.body() != null) {
                                errorMsg = response.body().getMessage();
                            }
                        } catch (Exception e) {
                            errorMsg = "Lỗi hệ thống: " + response.code();
                        }
                        toast(errorMsg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                    toast("Lỗi kết nối: " + t.getMessage());
                }
            });
        });

        view.findViewById(R.id.btn_close_detail).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDatePicker(EditText target) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày sinh").setTheme(R.style.CustomDatePickerTheme).build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            target.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        });
        datePicker.show(getSupportFragmentManager(), "DOB_PICKER");
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("Khách")) return "?";
        String[] words = name.trim().split("\\s+");
        if (words.length == 0) return "?";
        if (words.length == 1) return words[0].substring(0, 1).toUpperCase();
        return words[0].substring(0, 1).toUpperCase() + words[words.length - 1].substring(0, 1).toUpperCase();
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
