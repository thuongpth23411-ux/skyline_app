package com.skyline.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends BaseAuthActivity {
    private boolean isLengthValid = false;
    private boolean isComplexValid = false;
    private boolean isSpaceValid = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        sessionManager = new SessionManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        EditText edtOld = findViewById(R.id.edtOldPassword);
        EditText edtNew = findViewById(R.id.edtNewPassword);
        EditText edtConfirm = findViewById(R.id.edtConfirmPassword);

        TextView tvReqLength = findViewById(R.id.tvReqLength);
        TextView tvReqComplex = findViewById(R.id.tvReqComplex);
        TextView tvReqSpace = findViewById(R.id.tvReqSpace);

        setupPasswordVisibility(edtOld);
        setupPasswordVisibility(edtNew);
        setupPasswordVisibility(edtConfirm);

        edtNew.addTextChangedListener(new TextWatcher() {
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

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            String oldPass = edtOld.getText().toString();
            String newPass = edtNew.getText().toString();
            String confirm = edtConfirm.getText().toString();

            if (oldPass.isEmpty()) {
                toast("Vui lòng nhập mật khẩu cũ");
                return;
            }

            if (!isLengthValid || !isComplexValid || !isSpaceValid) {
                showErrorDialog("Mật khẩu mới chưa đáp ứng đủ yêu cầu bảo mật");
                return;
            }

            if (!newPass.equals(confirm)) {
                showErrorDialog("Mật khẩu xác nhận không khớp");
                return;
            }

            performChangePassword(oldPass, newPass);
        });
    }

    private void performChangePassword(String oldPass, String newPass) {
        String token = "Bearer " + sessionManager.fetchAuthToken();
        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPass);
        body.put("newPassword", newPass);

        RetrofitClient.getInstance().changePassword(token, body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    toast("Đổi mật khẩu thành công");
                    finish();
                } else {
                    String msg = "Lỗi hệ thống: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(errorJson).getAsJsonObject();
                            if (jsonObject.has("message")) {
                                msg = jsonObject.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}
                    showErrorDialog(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                showErrorDialog("Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng.");
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
