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
}
