package com.skyline.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.skyline.app.NotificationActivity;
import com.skyline.app.PromotionsActivity;
import com.skyline.app.R;

public class NotificationHelper {

    public enum NotifType {
        PROMOTION, TICKET, PROFILE, SYSTEM
    }

    public static void showDropDownNotification(Activity activity, String title, String content, NotifType type, String targetData) {
        showDropDownNotification(activity, null, title, content, type, targetData);
    }

    public static void showDropDownNotification(Activity activity, String id, String title, String content, NotifType type, String targetData) {
        if (activity == null || activity.isFinishing()) return;

        SessionManager sm = new SessionManager(activity);

        // Chỉ hiện thông báo nếu đã đăng nhập và là thông báo thật (có id)
        if (id != null && !id.isEmpty()) {
            if (!sm.isLoggedIn()) return;
            if (sm.isNotificationReceived(id)) return;
            sm.markNotificationAsReceived(id);
        }

        // Tạo Dialog popup
        android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_custom_notification, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvContent = dialogView.findViewById(R.id.tv_dialog_content);
        ImageView imgIcon = dialogView.findViewById(R.id.img_dialog_icon);
        com.google.android.material.button.MaterialButton btnAction = dialogView.findViewById(R.id.btn_dialog_action);
        View btnClose = dialogView.findViewById(R.id.btn_dialog_close);

        tvTitle.setText(title);
        tvContent.setText(content);

        // Đổi icon và màu sắc theo loại
        int iconRes = R.drawable.ic_notifications;
        int colorRes = R.color.skyline_blue_dark;

        switch (type) {
            case PROMOTION: 
                iconRes = R.drawable.ic_gift; 
                btnAction.setText("XEM ƯU ĐÃI");
                break;
            case TICKET: 
                iconRes = R.drawable.ic_ticket; 
                btnAction.setText("XEM VÉ");
                break;
            case PROFILE: 
                iconRes = R.drawable.ic_profile; 
                btnAction.setText("XEM HỒ SƠ");
                break;
            case SYSTEM:
                if (title.toLowerCase().contains("lỗi") || title.toLowerCase().contains("thất bại")) {
                    colorRes = R.color.auth_error;
                }
                btnAction.setText("ĐỒNG Ý");
                break;
        }
        
        if (imgIcon != null) {
            imgIcon.setImageResource(iconRes);
            imgIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activity.getResources().getColor(colorRes)));
        }
        btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activity.getResources().getColor(colorRes)));

        btnAction.setOnClickListener(v -> {
            dialog.dismiss();
            
            // Nếu là thông báo hệ thống thông thường, không cần mở trang chi tiết
            if (type == NotifType.SYSTEM) return;

            // Mở màn hình chi tiết thông báo
            Intent intent = new Intent(activity, com.skyline.app.NotificationDetailActivity.class);
            com.skyline.model.Notification notif = new com.skyline.model.Notification(id, title, content, 
                new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date()), 
                type.name(), targetData);
            intent.putExtra("notification", notif);
            activity.startActivity(intent);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        activity.runOnUiThread(dialog::show);
        
        // Lưu vào danh sách hiển thị nếu có id
        if (id != null && !id.isEmpty()) {
            sm.addLocalNotification(id, title, content, type.name(), targetData);
        }
    }

    public static void showSimpleDialog(Activity activity, String title, String content) {
        showDropDownNotification(activity, null, title, content, NotifType.SYSTEM, null);
    }

    public static void handleNotificationClick(Activity activity, NotifType type, String targetData) {
        Intent intent;
        switch (type) {
            case PROMOTION:
                intent = new Intent(activity, PromotionsActivity.class);
                if (targetData != null) intent.putExtra("OPEN_PROMO_NAME", targetData);
                break;
            case TICKET:
                intent = new Intent(activity, com.skyline.app.HomeActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
                break;
            case PROFILE:
                intent = new Intent(activity, com.skyline.app.ProfileActivity.class);
                break;
            default:
                intent = new Intent(activity, NotificationActivity.class);
                break;
        }
        activity.startActivity(intent);
    }
}
