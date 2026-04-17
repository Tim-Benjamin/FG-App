package com.farego.app.utils;
// ============================================================
// FILE: app/src/main/java/com/farego/app/utils/SessionManager.java
// PURPOSE: Persists login session via SharedPreferences.
//          SplashActivity reads this to route to Home or Auth.
// ============================================================

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME    = "farego_session";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final int    NO_USER      = -1;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Call after successful login */
    public void saveSession(int userId, String username, boolean isAdmin) {
        prefs.edit()
                .putInt(KEY_USER_ID,  userId)
                .putString(KEY_USERNAME, username)
                .putBoolean(KEY_IS_ADMIN, isAdmin)
                .apply();
    }

    /** Returns stored userId, or -1 if no session */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /** True if a session token exists (still need DB null-safety check at splash) */
    public boolean isLoggedIn() {
        return prefs.getInt(KEY_USER_ID, NO_USER) != NO_USER;
    }

    /** Call on logout */
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}