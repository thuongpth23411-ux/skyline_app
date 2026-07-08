package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.ForgotPasswordRequest;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.VerifyOtpRequest;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotOtpActivity extends BaseAuthActivity {
    private TextView tvResend;
    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        setupHomeButton();

        String email = getIntent().getStringExtra("EMAIL");
        
        TextView tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText(email);

        tvResend = findViewById(R.id.tvResend);
        startResendTimer();

        tvResend.setOnClickListener(v -> {
            if (isResendEnabled) {
                resendOtp(email);
            }
        });

        List<EditText> otpInputs = Arrays.asList(
            findViewById(R.id.edtOtp1), findViewById(R.id.edtOtp2),
            findViewById(R.id.edtOtp3), findViewById(R.id.edtOtp4),
            findViewById(R.id.edtOtp5), findViewById(R.id.edtOtp6)
        );
        setupOtpInputs(otpInputs);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnVerify).setOnClickListener(v -> {
            StringBuilder otp = new StringBuilder();
            for (EditText input : otpInputs) {
                otp.append(input.getText().toString());
            }

            if (otp.length() < 6) {
                showErrorDialog("Vui lòng nhập đầy đủ mã OTP");
                return;
            }

            RetrofitClient.getInstance().verifyOtp(new VerifyOtpRequest(email, otp.toString())).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Intent intent = new Intent(ForgotOtpActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("OTP", otp.toString());
                        startActivity(intent);
                    } else {
                        showErrorDialog(response.body() != null ? response.body().getMessage() : "Xác thực thất bại");
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    showErrorDialog("Lỗi kết nối: " + t.getMessage());
                }
            });
        });
    }

    private void startResendTimer() {
        isResendEnabled = false;
        tvResend.setTextColor(getResources().getColor(R.color.auth_hint));
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResend.setText("Gửi lại mã sau " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                isResendEnabled = true;
                tvResend.setText("Gửi lại mã");
                tvResend.setTextColor(getResources().getColor(R.color.auth_blue));
            }
        }.start();
    }

    private void resendOtp(String email) {
        RetrofitClient.getInstance().forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ForgotOtpActivity.this, "Mã mới đã được gửi", Toast.LENGTH_SHORT).show();
                    startResendTimer();
                } else {
                    showErrorDialog(response.body() != null ? response.body().getMessage() : "Không thể gửi lại mã");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                showErrorDialog("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
