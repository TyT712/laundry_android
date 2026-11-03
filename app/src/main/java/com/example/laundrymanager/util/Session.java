package com.example.laundrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.laundrymanager.model.Role;
import com.example.laundrymanager.model.User;

/** Phiên đăng nhập của người dùng hiện tại. */
public final class Session {
    private static final String PREFS = "laundry_session";
    private static final String KEY_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    public Session(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void start(User user) {
        prefs.edit()
                .putInt(KEY_ID, user.id())
                .putString(KEY_USERNAME, user.username())
                .putString(KEY_FULLNAME, user.fullname())
                .putString(KEY_ROLE, user.role().name())
                .apply();
    }

    public void updateFullname(String fullname) {
        prefs.edit().putString(KEY_FULLNAME, fullname).apply();
    }

    public void end() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_ID);
    }

    public int userId() {
        return prefs.getInt(KEY_ID, 0);
    }

    public String username() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String fullname() {
        return prefs.getString(KEY_FULLNAME, "");
    }

    public Role role() {
        return Role.valueOf(prefs.getString(KEY_ROLE, Role.STAFF.name()));
    }

    public boolean isManager() {
        return role() == Role.MANAGER;
    }
}

