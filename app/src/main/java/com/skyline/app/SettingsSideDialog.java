package com.skyline.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.skyline.app.databinding.ActivitySettingsBinding;
import com.skyline.app.utils.SessionManager;

public class SettingsSideDialog extends DialogFragment {

    private ActivitySettingsBinding binding;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    sessionManager.setNotificationsEnabled(true);
                    updateSwitchUI(true);
                    Toast.makeText(getContext(), "Đã bật thông báo thành công", Toast.LENGTH_SHORT).show();
                } else {
                    sessionManager.setNotificationsEnabled(false);
                    updateSwitchUI(false);
                    showSettingsRedirectDialog("Bạn đã từ chối cấp quyền. Để nhận được các ưu đãi mới nhất, vui lòng bật thông báo trong Cài đặt máy.");
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomSideDialogTheme);
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Window window = getDialog() != null ? getDialog().getWindow() : null;
        if (window != null) {
            window.setGravity(Gravity.END);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        // Kiểm tra thực tế từ hệ thống trước, sau đó mới đến preference
        boolean isSystemEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
        boolean isPrefEnabled = sessionManager.areNotificationsEnabled();
        
        updateSwitchUI(isSystemEnabled && isPrefEnabled);
    }

    private void updateSwitchUI(boolean isEnabled) {
        if (binding != null) {
            binding.switchNotifications.setOnCheckedChangeListener(null);
            binding.switchNotifications.setChecked(isEnabled);
            setupSwitchListener();
        }
    }

    private void setupListeners() {
        binding.rootLayout.setOnClickListener(v -> dismiss());
        binding.sidePanel.setOnClickListener(v -> {});
        binding.btnBack.setOnClickListener(v -> dismiss());

        setupSwitchListener();

        binding.btnLanguage.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Tính năng chọn ngôn ngữ đang phát triển", Toast.LENGTH_SHORT).show()
        );

        binding.btnCurrency.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Tính năng chọn tiền tệ đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupSwitchListener() {
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
            } else {
                sessionManager.setNotificationsEnabled(false);
                Toast.makeText(getContext(), "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRequestNotificationPermission() {
        // 1. Kiểm tra xem thông báo có bị tắt ở cấp độ Cài đặt máy hay không (Dành cho mọi đời máy)
        if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
            updateSwitchUI(false);
            showSettingsRedirectDialog("Thông báo hiện đang bị tắt trong cài đặt hệ thống. Vui lòng mở lại để không bỏ lỡ các ưu đãi từ Skyline.");
            return;
        }

        // 2. Nếu hệ thống cho phép, tiếp tục xử lý quyền Runtime (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                sessionManager.setNotificationsEnabled(true);
                Toast.makeText(getContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Android 12 trở xuống: Chỉ cần lưu preference vì quyền mặc định đã có
            sessionManager.setNotificationsEnabled(true);
            Toast.makeText(getContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettingsRedirectDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Thông báo hệ thống")
                .setMessage(message)
                .setPositiveButton("Đi tới Cài đặt", (dialog, which) -> {
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                    } else {
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Để sau", (dialog, which) -> updateSwitchUI(false))
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
