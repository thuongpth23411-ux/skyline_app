package com.skyline.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.skyline.app.network.AuthResponse;
import com.skyline.app.network.RegisterRequest;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompleteInfoActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_info);
        setupHomeButton();

        String email = getIntent().getStringExtra("EMAIL");
        String password = getIntent().getStringExtra("PASSWORD");

        TextView tvCountryCode = findViewById(R.id.tvCountryCode);
        EditText edtPhone = findViewById(R.id.edtPhone);
        TextView tvTitle = findViewById(R.id.tvTitle);
        EditText edtFirstName = findViewById(R.id.edtFirstName);
        EditText edtLastName = findViewById(R.id.edtLastName);
        EditText edtDob = findViewById(R.id.edtDob);
        EditText edtCccd = findViewById(R.id.edtCccd);
        EditText edtPassport = findViewById(R.id.edtPassport);
        TextView tvCountry = findViewById(R.id.tvCountry);
        CheckBox cbAgree = findViewById(R.id.cbAgree);

        tvCountryCode.setOnClickListener(v -> showListSelector("Chọn mã vùng", new String[]{"+84 (Việt Nam)", "+66 (Thái Lan)", "+65 (Singapore)", "+60 (Malaysia)"}, selected -> {
            tvCountryCode.setText(selected.split(" ")[0]);
        }));

        tvTitle.setOnClickListener(v -> showListSelector("Chọn danh xưng", new String[]{"Ông", "Bà"}, tvTitle::setText));

        tvCountry.setOnClickListener(v -> showListSelector("Chọn quốc gia", new String[]{"Việt Nam", "Thái Lan", "Singapore", "Malaysia", "Hàn Quốc", "Nhật Bản"}, tvCountry::setText));

        edtDob.setOnClickListener(v -> showDatePicker(edtDob));

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            String dob = edtDob.getText().toString().trim();
            String cccd = edtCccd.getText().toString().trim();
            String passport = edtPassport.getText().toString().trim();

            if (!dob.isEmpty() && !isValidDate(dob)) {
                showErrorDialog("Ngày sinh không đúng định dạng DD/MM/YYYY");
                return;
            }

            if (!phone.isEmpty() && !isValidPhone(phone)) {
                showErrorDialog("Số điện thoại không hợp lệ (yêu cầu 10-11 chữ số)");
                return;
            }

            if (!cccd.isEmpty() && !isValidCccd(cccd)) {
                showErrorDialog("Số CCCD không hợp lệ (yêu cầu đúng 12 chữ số)");
                return;
            }

            if (!passport.isEmpty() && !isValidPassport(passport)) {
                showErrorDialog("Số hộ chiếu không hợp lệ");
                return;
            }
            
            if (!cbAgree.isChecked()) {
                showErrorDialog("Vui lòng đồng ý với các điều khoản để tiếp tục");
                return;
            }

            String fullName = (edtFirstName.getText().toString().trim() + " " + edtLastName.getText().toString().trim()).trim();
            if (fullName.isEmpty()) fullName = "User";

            v.setEnabled(false);
            String country = tvCountry.getText().toString();
            String title = tvTitle.getText().toString();
            String countryCode = tvCountryCode.getText().toString();
            
            RegisterRequest request = new RegisterRequest(
                email, 
                password, 
                fullName, 
                phone.isEmpty() ? null : (countryCode + phone),
                cccd.isEmpty() ? null : cccd,
                passport.isEmpty() ? null : passport,
                dob.isEmpty() ? null : dob,
                country.equals("-- Chọn quốc gia --") ? null : country,
                title.equals("Bà") || title.equals("Ông") ? title : null,
                null // Address field if you have it in UI
            );

            RetrofitClient.getInstance().registerFinalize(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    v.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        AuthResponse body = response.body();
                        if (body.getToken() != null) {
                            SessionManager sessionManager = new SessionManager(CompleteInfoActivity.this);
                            sessionManager.saveAuthToken(body.getToken());
                            String name = body.getUser() != null ? body.getUser().getName() : null;
                            sessionManager.saveUser(name, email);
                        }
                        Toast.makeText(CompleteInfoActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CompleteInfoActivity.this, AccountSuccessActivity.class));
                        finishAffinity();
                    } else {
                        showErrorDialog(response.body() != null ? response.body().getMessage() : "Đăng ký thất bại");
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    v.setEnabled(true);
                    showErrorDialog("Lỗi kết nối: " + t.getMessage());
                }
            });
        });

        findViewById(R.id.tvSkip).setOnClickListener(v -> {
            v.setEnabled(false);
            RegisterRequest request = new RegisterRequest(email, password);
            RetrofitClient.getInstance().registerFinalize(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    v.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        AuthResponse body = response.body();
                        if (body.getToken() != null) {
                            SessionManager sessionManager = new SessionManager(CompleteInfoActivity.this);
                            sessionManager.saveAuthToken(body.getToken());
                            String name = body.getUser() != null ? body.getUser().getName() : null;
                            sessionManager.saveUser(name, email);
                        }
                        startActivity(new Intent(CompleteInfoActivity.this, AccountSuccessActivity.class));
                        finishAffinity();
                    } else {
                        showErrorDialog(response.body() != null ? response.body().getMessage() : "Đăng ký thất bại");
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    v.setEnabled(true);
                    showErrorDialog("Lỗi kết nối: " + t.getMessage());
                }
            });
        });
    }

    private void showListSelector(String title, String[] items, OnSelectedListener onSelected) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(items, (dialog, which) -> onSelected.onSelected(items[which]))
            .show();
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y);
            editText.setText(date);
        }, year, month, day).show();
    }

    private boolean isValidDate(String date) {
        return date.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d$");
    }

    private boolean isValidPhone(String phone) {
        return phone.length() >= 10 && phone.length() <= 11 && phone.matches("\\d+");
    }

    private boolean isValidCccd(String cccd) {
        return cccd.length() == 12 && cccd.matches("\\d+");
    }

    private boolean isValidPassport(String passport) {
        return passport.length() >= 7 && passport.length() <= 15 && passport.matches("[a-zA-Z0-9]+");
    }

    interface OnSelectedListener {
        void onSelected(String selected);
    }
}
