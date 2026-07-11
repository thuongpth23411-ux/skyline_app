package com.skyline.app;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import java.util.List;

public class BaseAuthActivity extends AppCompatActivity {
    protected void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    protected void setupHomeButton() {
        View btnHome = findViewById(R.id.btnHome);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> goHome());
        }
    }

    protected void setupPasswordVisibility(EditText editText) {
        final boolean[] isPasswordVisible = {false};
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editText.getCompoundDrawables()[2] != null) {
                    if (event.getX() >= (editText.getWidth() - editText.getPaddingEnd() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        isPasswordVisible[0] = !isPasswordVisible[0];
                        if (isPasswordVisible[0]) {
                            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            editText.setCompoundDrawablesWithIntrinsicBounds(editText.getCompoundDrawables()[0], null, AppCompatResources.getDrawable(this, R.drawable.ic_eye_auth), null);
                        } else {
                            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            editText.setCompoundDrawablesWithIntrinsicBounds(editText.getCompoundDrawables()[0], null, AppCompatResources.getDrawable(this, R.drawable.ic_eye_off_auth), null);
                        }
                        editText.setSelection(editText.getText().length());
                        v.performClick();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    protected void setupOtpInputs(List<EditText> inputs) {
        for (int i = 0; i < inputs.size(); i++) {
            final int index = i;
            inputs.get(i).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null && s.length() == 1 && index < inputs.size() - 1) {
                        inputs.get(index + 1).requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            inputs.get(i).setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    if (inputs.get(index).getText().length() == 0 && index > 0) {
                        inputs.get(index - 1).requestFocus();
                        inputs.get(index - 1).setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    protected void updateRequirementStatus(TextView textView, boolean isValid) {
        int color = isValid ? getColor(R.color.auth_success) : getColor(R.color.auth_hint);
        textView.setTextColor(color);
        if (textView.getCompoundDrawablesRelative()[0] != null) {
            textView.getCompoundDrawablesRelative()[0].setTint(color);
        }
    }

    protected void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Thông báo")
            .setMessage(message)
            .setPositiveButton("Đồng ý", null)
            .show();
    }
}
