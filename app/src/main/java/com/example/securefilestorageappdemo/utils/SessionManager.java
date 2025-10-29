// utils/SessionManager.java
package com.example.securefilestorageappdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USERNAME = "username";

    public static void saveUser(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public static String getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, null);
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USERNAME).apply();
    }
}
