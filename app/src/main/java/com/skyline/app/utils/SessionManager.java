package com.skyline.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private static final String PREF_NAME = "skyline_prefs";
    public static final String USER_TOKEN = "user_token";
    public static final String USER_NAME = "user_name";
    public static final String USER_EMAIL = "user_email";

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

    public void saveUser(String name, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_NAME, name);
        editor.putString(USER_EMAIL, email);
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
}
