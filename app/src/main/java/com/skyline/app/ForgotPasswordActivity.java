package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.ForgotPasswordRequest;
import com.skyline.app.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setupHomeButton();

        EditText edtEmail = findViewById(R.id.edtEmail);

        findViewById(R.id.btnOtp).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                showErrorDialog("Vui lòng nhập email");
                return;
            }

            RetrofitClient.getInstance().forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, ForgotOtpActivity.class);
                        intent.putExtra("EMAIL", email);
                        startActivity(intent);
                    } else {
                        showErrorDialog(response.body() != null ? response.body().getMessage() : "Gửi OTP thất bại");
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    showErrorDialog("Lỗi gửi OTP!\nChi tiết: " + t.getMessage());
                }
            });
        });

        findViewById(R.id.tvBackLogin).setOnClickListener(v -> finish());
    }
}
