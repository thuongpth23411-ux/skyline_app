package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.ForgotPasswordRequest;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.NotificationHelper;
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
                NotificationHelper.showSimpleDialog(this, "Thông báo", "Vui lòng nhập email");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                NotificationHelper.showSimpleDialog(this, "Lỗi", "Email không hợp lệ. Vui lòng kiểm tra lại.");
                return;
            }

            v.setEnabled(false);
            RetrofitClient.getInstance().sendOtpReg(new ForgotPasswordRequest(email)).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    v.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        NotificationHelper.showSimpleDialog(RegisterEmailActivity.this, "Thành công", "Mã OTP đã được gửi đến email");
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
                        NotificationHelper.showSimpleDialog(RegisterEmailActivity.this, "Lỗi", errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    v.setEnabled(true);
                    NotificationHelper.showSimpleDialog(RegisterEmailActivity.this, "Lỗi kết nối", "Lỗi kết nối: " + t.getMessage());
                }
            });
        });
    }

    private void showRegisteredEmailDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.dialog_custom_notification, null);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        ((android.widget.TextView)view.findViewById(R.id.tv_dialog_title)).setText("Thông báo");
        ((android.widget.TextView)view.findViewById(R.id.tv_dialog_content)).setText("Email này đã được đăng ký. Bạn có muốn chuyển qua trang đăng nhập?");
        
        com.google.android.material.button.MaterialButton btnLogin = view.findViewById(R.id.btn_dialog_action);
        btnLogin.setText("ĐĂNG NHẬP");
        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(RegisterEmailActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        view.findViewById(R.id.btn_dialog_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
