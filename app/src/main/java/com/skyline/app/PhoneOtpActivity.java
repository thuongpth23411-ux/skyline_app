package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.VerifyOtpRequest;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneOtpActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        setupHomeButton();

        String email = getIntent().getStringExtra("EMAIL");
        boolean isRegister = getIntent().getBooleanExtra("IS_REGISTER", false);

        List<EditText> otpInputs = Arrays.asList(
            findViewById(R.id.edtOtp1), findViewById(R.id.edtOtp2),
            findViewById(R.id.edtOtp3), findViewById(R.id.edtOtp4),
            findViewById(R.id.edtOtp5), findViewById(R.id.edtOtp6)
        );
        setupOtpInputs(otpInputs);

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
                        if (isRegister) {
                            Intent intent = new Intent(PhoneOtpActivity.this, SetPasswordActivity.class);
                            intent.putExtra("EMAIL", email);
                            intent.putExtra("OTP", otp.toString());
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(PhoneOtpActivity.this, AccountSuccessActivity.class));
                        }
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Xác thực thất bại";
                        showErrorDialog(errorMsg);
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
