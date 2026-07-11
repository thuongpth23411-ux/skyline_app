package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.ForgotPasswordRequest;
import com.skyline.app.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterEmailActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);
        setupHomeButton();

        EditText edtEmail = findViewById(R.id.edtEmail);

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                showErrorDialog("Vui lòng nhập email");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showErrorDialog("Email không hợp lệ. Vui lòng kiểm tra lại.");
                return;
            }

            v.setEnabled(false);
            RetrofitClient.getInstance().sendOtpReg(new ForgotPasswordRequest(email)).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    v.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(RegisterEmailActivity.this, "Mã OTP đã được gửi đến email", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterEmailActivity.this, PhoneOtpActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("IS_REGISTER", true);
                        startActivity(intent);
                    } else {
                        // Kiểm tra nếu Email đã tồn tại (Lỗi 400 từ Backend)
                        if (response.code() == 400) {
                            try {
                                String errorBody = response.errorBody().string();
                                if (errorBody.contains("Email đã được đăng ký")) {
                                    showRegisteredEmailDialog();
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Gửi OTP thất bại";
                        showErrorDialog(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    v.setEnabled(true);
                    showErrorDialog("Lỗi kết nối: " + t.getMessage());
                }
            });
        });
    }

    private void showRegisteredEmailDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Thông báo")
            .setMessage("Email này đã được đăng ký. Bạn có muốn chuyển qua trang đăng nhập?")
            .setPositiveButton("Đăng nhập", (dialog, which) -> {
                Intent intent = new Intent(RegisterEmailActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}
