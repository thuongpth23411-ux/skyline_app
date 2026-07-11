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

        // Chỉ hiện thông báo nếu đã đăng nhập
        if (!sm.isLoggedIn()) {
            return;
        }
        
        // Nếu có ID và đã nhận rồi thì không hiện nữa
        if (id != null && !id.isEmpty()) {
            if (sm.isNotificationReceived(id)) {
                return;
            }
            // Đánh dấu đã nhận NGAY LẬP TỨC để tránh lặp khi gọi post/delay
            sm.markNotificationAsReceived(id);
        }

        // Tạo Dialog popup thay vì PopupWindow
        android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_custom_notification, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvContent = dialogView.findViewById(R.id.tv_dialog_content);
        ImageView imgIcon = dialogView.findViewById(R.id.img_dialog_icon);
        View btnAction = dialogView.findViewById(R.id.btn_dialog_action);
        View btnClose = dialogView.findViewById(R.id.btn_dialog_close);

        tvTitle.setText(title);
        tvContent.setText(content);

        // Đổi icon theo loại
        int iconRes = R.drawable.ic_notifications;
        switch (type) {
            case PROMOTION: iconRes = R.drawable.ic_gift; break;
            case TICKET: iconRes = R.drawable.ic_ticket; break;
            case PROFILE: iconRes = R.drawable.ic_profile; break;
        }
        if (imgIcon != null) imgIcon.setImageResource(iconRes);

        btnAction.setOnClickListener(v -> {
            dialog.dismiss();
            
            // Mở màn hình chi tiết thông báo
            Intent intent = new Intent(activity, com.skyline.app.NotificationDetailActivity.class);
            com.skyline.model.Notification notif = new com.skyline.model.Notification(id, title, content, 
                new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date()), 
                type.name(), targetData);
            intent.putExtra("notification", notif);
            activity.startActivity(intent);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        activity.runOnUiThread(() -> {
            dialog.show();
            // Lưu vào danh sách hiển thị
            sm.addLocalNotification(id, title, content, type.name(), targetData);
        });
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
