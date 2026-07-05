package com.skyline.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private static final String PREF_NAME = "skyline_prefs";
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
    
    public String getUserName() { return prefs.getString(USER_NAME, "Khách"); }
    public String getMemberCode() { return prefs.getString(USER_MEMBER_CODE, "---- ---- ----"); }
}
