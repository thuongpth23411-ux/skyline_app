package com.skyline.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private static final String PREF_NAME = "skyline_prefs";
    public static final String USER_TOKEN = "user_token";
    public static final String USER_ID = "user_id";
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

    public String getUserId() {
        return prefs.getString(USER_ID, null);
    }

    public boolean isLoggedIn() {
        return fetchAuthToken() != null;
    }

    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    public String getUserName() { return prefs.getString(USER_NAME, "Khách"); }
    public String getUserEmail() { return prefs.getString(USER_EMAIL, "vynnt23411@st.uel.edu.vn"); }
    public String getMemberCode() { return prefs.getString(USER_MEMBER_CODE, "---- ---- ----"); }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean("notif_enabled", true);
    }

    public void addLocalNotification(String title, String content) {
        addLocalNotification(null, title, content, "SYSTEM", null);
    }

    public void addLocalNotification(String id, String title, String content, String type, String data) {
        String existing = prefs.getString("local_notifs", "");
        String time = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
        // Format: id|title|content|time|type|targetData;
        String entry = (id != null ? id : "") + "|" + title + "|" + content + "|" + time + "|" + (type != null ? type : "") + "|" + (data != null ? data : "") + ";";
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("local_notifs", entry + existing);
        
        // Tăng đếm thông báo chưa đọc
        int currentCount = getUnreadNotifCount();
        editor.putInt("unread_notif_count", currentCount + 1);
        
        editor.apply();
    }

    public String getLocalNotifications() {
        return prefs.getString("local_notifs", "");
    }

    public int getUnreadNotifCount() {
        return prefs.getInt("unread_notif_count", 0);
    }

    public void clearUnreadNotifCount() {
        prefs.edit().putInt("unread_notif_count", 0).apply();
    }

    public void clearNotifications() {
        prefs.edit().putString("local_notifs", "").apply();
    }

    public boolean isNotificationReceived(String id) {
        Set<String> received = prefs.getStringSet("received_notif_ids", new HashSet<>());
        return received.contains(id);
    }

    public void markNotificationAsReceived(String id) {
        Set<String> received = new HashSet<>(prefs.getStringSet("received_notif_ids", new HashSet<>()));
        received.add(id);
        prefs.edit().putStringSet("received_notif_ids", received).apply();
    }
}
