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
    public static final String USER_GENDER = "user_gender";
    public static final String USER_POINTS = "user_points";
    public static final String USER_AVATAR = "user_avatar";

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
        editor.putString(USER_GENDER, user.getGender());
        editor.putInt(USER_POINTS, user.getSkyPoints());
        editor.putString(USER_AVATAR, user.getAvatarUrl());
        
        // Save full object as JSON for easy retrieval
        editor.putString("user_json", new com.google.gson.Gson().toJson(user));
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(USER_ID, null);
    }

    public String getUserAvatar() {
        return prefs.getString(USER_AVATAR, null);
    }

    public com.skyline.app.network.User getUser() {
        String json = prefs.getString("user_json", null);
        if (json == null) return null;
        return new com.google.gson.Gson().fromJson(json, com.skyline.app.network.User.class);
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
    public String getUserGender() { return prefs.getString(USER_GENDER, ""); }
    public int getUserPoints() { return prefs.getInt(USER_POINTS, 0); }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean("notif_enabled", true);
    }

    public void addLocalNotification(String title, String content) {
        addLocalNotification("", title, content, "SYSTEM", null);
    }

    public void addLocalNotification(String id, String title, String content, String type, String targetData) {
        String existing = prefs.getString("local_notifs", "");
        String time = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
        
        String entry = (id == null ? "" : id) + "|" + 
                       title + "|" + 
                       content + "|" + 
                       time + "|" + 
                       (type == null ? "SYSTEM" : type) + "|" + 
                       (targetData == null ? "" : targetData) + ";";
                       
        prefs.edit().putString("local_notifs", entry + existing).apply();
        setUnreadNotifCount(getUnreadNotifCount() + 1);
    }

    public String getLocalNotifications() {
        return prefs.getString("local_notifs", "");
    }

    public void clearNotifications() {
        prefs.edit().remove("local_notifs").apply();
    }

    public int getUnreadNotifCount() {
        return prefs.getInt("unread_notif_count", 0);
    }

    public void setUnreadNotifCount(int count) {
        prefs.edit().putInt("unread_notif_count", count).apply();
    }

    public void clearUnreadNotifCount() {
        prefs.edit().putInt("unread_notif_count", 0).apply();
    }

    public boolean isNotificationReceived(String id) {
        if (id == null || id.isEmpty()) return false;
        return prefs.getBoolean("notif_received_" + id, false);
    }

    public void markNotificationAsReceived(String id) {
        if (id == null || id.isEmpty()) return;
        prefs.edit().putBoolean("notif_received_" + id, true).apply();
    }
}
