package com.skyline.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private static final String PREF_NAME = "skyline_prefs";
    public static final String USER_ID = "user_id";
    public static final String USER_TOKEN = "user_token";
    public static final String USER_NAME = "user_name";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_MEMBER_CODE = "user_member_code";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_RANK = "user_rank";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_TOKEN, token);
        editor.apply();
    }

    public String fetchAuthToken() {
        return prefs.getString(USER_TOKEN, null);
    }

    public void saveUser(com.skyline.app.network.User user) {
        if (user == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_ID, user.getId());
        editor.putString(USER_NAME, user.getName());
        editor.putString(USER_EMAIL, user.getEmail());
        editor.putString(USER_MEMBER_CODE, user.getMemberCode());
        editor.putString(USER_PHONE, user.getPhone());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return fetchAuthToken() != null;
    }

    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    public String getUserId() { return prefs.getString(USER_ID, null); }
    public String getUserName() { return prefs.getString(USER_NAME, "Khách"); }
    public String getUserEmail() { return prefs.getString(USER_EMAIL, ""); }
    public String getMemberCode() { return prefs.getString(USER_MEMBER_CODE, "---- ---- ----"); }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean("notif_enabled", true);
    }

    public void addLocalNotification(String title, String content) {
        addLocalNotification(null, title, content, null, null);
    }

    public void addLocalNotification(String id, String title, String content, String type, String targetData) {
        String existing = prefs.getString("local_notifs", "");
        String time = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
        
        // Format: id|title|content|time|type|targetData;
        String entry = (id != null ? id : "") + "|" + 
                       title + "|" + 
                       content + "|" + 
                       time + "|" + 
                       (type != null ? type : "") + "|" + 
                       (targetData != null ? targetData : "") + ";";

        prefs.edit().putString("local_notifs", entry + existing).apply();
        
        // Tăng số lượng thông báo chưa đọc
        int unreadCount = prefs.getInt("unread_notif_count", 0);
        prefs.edit().putInt("unread_notif_count", unreadCount + 1).apply();
        
        // Đánh dấu đã nhận nếu có ID
        if (id != null && !id.isEmpty()) {
            markNotificationAsReceived(id);
        }
    }

    public void markNotificationAsReceived(String id) {
        String userId = getUserId();
        if (userId == null) userId = "guest";
        String key = "received_notif_" + userId + "_" + id;
        prefs.edit().putBoolean(key, true).commit();
    }

    public boolean isNotificationReceived(String id) {
        String userId = getUserId();
        if (userId == null) userId = "guest";
        String key = "received_notif_" + userId + "_" + id;
        return prefs.getBoolean(key, false);
    }

    public int getUnreadNotifCount() {
        return prefs.getInt("unread_notif_count", 0);
    }

    public void clearUnreadNotifCount() {
        prefs.edit().putInt("unread_notif_count", 0).apply();
    }

    public String getLocalNotifications() {
        return prefs.getString("local_notifs", "");
    }

    public void clearNotifications() {
        prefs.edit().remove("local_notifs").apply();
        clearUnreadNotifCount();
    }
}
