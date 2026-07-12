package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.skyline.app.network.AuthResponse;
import com.skyline.app.network.LoginRequest;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import com.skyline.app.utils.NotificationHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseAuthActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupHomeButton();

        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPassword = findViewById(R.id.edtPassword);
        setupPasswordVisibility(edtPassword);

        findViewById(R.id.tvForgot).setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        findViewById(R.id.tvRegister).setOnClickListener(v -> startActivity(new Intent(this, RegisterEmailActivity.class)));

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                NotificationHelper.showSimpleDialog(this, "Thông báo", "Vui lòng nhập đầy đủ thông tin");
                return;
            }

            RetrofitClient.getInstance().login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        AuthResponse authResponse = response.body();
                        if (authResponse.getToken() != null) {
                            SessionManager sessionManager = new SessionManager(LoginActivity.this);
                            sessionManager.saveAuthToken(authResponse.getToken());
                            sessionManager.saveUser(authResponse.getUser());
                        }
                        NotificationHelper.showSimpleDialog(LoginActivity.this, "Thành công", "Đăng nhập thành công");
                        goHome();
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Đăng nhập thất bại";
                        NotificationHelper.showSimpleDialog(LoginActivity.this, "Lỗi đăng nhập", errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    NotificationHelper.showSimpleDialog(LoginActivity.this, "Lỗi kết nối", "Lỗi kết nối server!\nChi tiết: " + t.getMessage());
                }
            });
        });
    }
}
