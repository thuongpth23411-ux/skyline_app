package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.ResetPasswordRequest;
import com.skyline.app.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends BaseAuthActivity {
    private boolean isLengthValid = false;
    private boolean isComplexValid = false;
    private boolean isSpaceValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        setupHomeButton();

        String email = getIntent().getStringExtra("EMAIL");
        String otp = getIntent().getStringExtra("OTP");

        EditText edtNewPass = findViewById(R.id.edtNewPass);
        EditText edtConfirm = findViewById(R.id.edtConfirm);
        
        TextView tvReqLength = findViewById(R.id.tvReqLength);
        TextView tvReqComplex = findViewById(R.id.tvReqComplex);
        TextView tvReqSpace = findViewById(R.id.tvReqSpace);

        setupPasswordVisibility(edtNewPass);
        setupPasswordVisibility(edtConfirm);

        edtNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s != null ? s.toString() : "";
                isLengthValid = password.length() >= 8;
                isComplexValid = password.matches(".*[A-Z].*") && 
                                 password.matches(".*[a-z].*") && 
                                 password.matches(".*[0-9].*") && 
                                 password.matches(".*[^a-zA-Z0-9].*");
                isSpaceValid = !password.isEmpty() && !password.contains(" ");

                updateRequirementStatus(tvReqLength, isLengthValid);
                updateRequirementStatus(tvReqComplex, isComplexValid);
                updateRequirementStatus(tvReqSpace, isSpaceValid);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            String newPass = edtNewPass.getText().toString();
            String confirm = edtConfirm.getText().toString();

            if (!isLengthValid || !isComplexValid || !isSpaceValid) {
                showErrorDialog("Mật khẩu chưa đáp ứng đủ yêu cầu");
                return;
            }

            if (!newPass.equals(confirm)) {
                showErrorDialog("Mật khẩu xác nhận không khớp");
                return;
            }

            RetrofitClient.getInstance().resetPassword(new ResetPasswordRequest(email, otp, newPass)).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, ResetSuccessActivity.class));
                    } else {
                        showErrorDialog(response.body() != null ? response.body().getMessage() : "Đổi mật khẩu thất bại");
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    showErrorDialog("Lỗi kết nối: " + t.getMessage());
                }
            });
        });

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());
    }
}
