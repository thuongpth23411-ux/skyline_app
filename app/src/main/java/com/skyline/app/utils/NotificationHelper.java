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

        LayoutInflater inflater = LayoutInflater.from(activity);
        View popupView = inflater.inflate(R.layout.layout_notification_popup, null);

        TextView tvTitle = popupView.findViewById(R.id.tv_notif_title);
        TextView tvDesc = popupView.findViewById(R.id.tv_notif_desc);
        ImageView imgIcon = popupView.findViewById(R.id.img_notif_icon); // Cần thêm ID này vào XML

        tvTitle.setText(title);
        tvDesc.setText(content);

        // Đổi icon theo loại
        int iconRes = R.drawable.ic_notifications;
        switch (type) {
            case PROMOTION: iconRes = R.drawable.ic_gift; break;
            case TICKET: iconRes = R.drawable.ic_ticket; break;
            case PROFILE: iconRes = R.drawable.ic_profile; break;
        }
        if (imgIcon != null) imgIcon.setImageResource(iconRes);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        popupView.setOnClickListener(v -> {
            popupWindow.dismiss();
            handleNotificationClick(activity, type, targetData);
        });

        popupView.findViewById(R.id.btn_close_popup).setOnClickListener(v -> popupWindow.dismiss());

        View rootView = activity.findViewById(android.R.id.content);
        rootView.post(() -> {
            popupWindow.showAtLocation(rootView, Gravity.TOP, 0, 50);
            
            // Lưu vào danh sách hiển thị
            sm.addLocalNotification(id, title, content, type.name(), targetData);
        });
    }

    private static void handleNotificationClick(Activity activity, NotifType type, String targetData) {
        Intent intent;
        switch (type) {
            case PROMOTION:
                intent = new Intent(activity, PromotionsActivity.class);
                if (targetData != null) intent.putExtra("OPEN_PROMO_NAME", targetData);
                break;
            case TICKET:
                // Giả sử có một trang xem vé hoặc HomeActivity -> Tab Flights
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
