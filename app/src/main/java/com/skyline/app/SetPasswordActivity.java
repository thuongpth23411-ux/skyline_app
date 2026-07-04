package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class SetPasswordActivity extends BaseAuthActivity {
    private boolean isLengthValid = false;
    private boolean isComplexValid = false;
    private boolean isSpaceValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        setupHomeButton();

        String email = getIntent().getStringExtra("EMAIL");
        EditText edtPassword = findViewById(R.id.edtPassword);
        EditText edtConfirm = findViewById(R.id.edtConfirm);
        
        TextView tvReqLength = findViewById(R.id.tvReqLength);
        TextView tvReqComplex = findViewById(R.id.tvReqComplex);
        TextView tvReqSpace = findViewById(R.id.tvReqSpace);

        setupPasswordVisibility(edtPassword);
        setupPasswordVisibility(edtConfirm);

        edtPassword.addTextChangedListener(new TextWatcher() {
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

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String password = edtPassword.getText().toString();
            String confirm = edtConfirm.getText().toString();

            if (!isLengthValid || !isComplexValid || !isSpaceValid) {
                showErrorDialog("Mật khẩu chưa đáp ứng đủ yêu cầu");
                return;
            }

            if (!password.equals(confirm)) {
                showErrorDialog("Mật khẩu xác nhận không khớp");
                return;
            }

            Intent intent = new Intent(this, CompleteInfoActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("PASSWORD", password);
            startActivity(intent);
        });
    }
}
