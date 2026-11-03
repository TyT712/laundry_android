package com.example.laundrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.laundrymanager.model.Role;
import com.example.laundrymanager.model.User;
import com.example.laundrymanager.util.PasswordHasher;

public final class UserRepository {
    private static final String COLUMNS = "id, username, fullname, phone, role";

    private final DBHelper helper;

    public UserRepository(Context context) {
        helper = DBHelper.getInstance(context);
    }

    /** @return người dùng nếu thông tin đăng nhập đúng, ngược lại {@code null}. */
    public User authenticate(String username, String password) {
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT " + COLUMNS + ", password FROM users WHERE username = ?", new String[]{username})) {
            if (c.moveToFirst() && PasswordHasher.matches(password, c.getString(5))) {
                return map(c);
            }
            return null;
        }
    }

    public User findById(int id) {
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT " + COLUMNS + " FROM users WHERE id = ?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? map(c) : null;
        }
    }

    /** Đăng ký tài khoản nhân viên mới. @return {@code false} nếu tên đăng nhập đã tồn tại. */
    public boolean registerStaff(String username, String password, String fullname, String phone) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", PasswordHasher.hash(password));
        values.put("fullname", fullname);
        values.put("phone", phone);
        values.put("role", Role.STAFF.name());
        return helper.getWritableDatabase().insert("users", null, values) != -1;
    }

    /** Cập nhật hồ sơ; {@code newPassword} rỗng nghĩa là giữ nguyên mật khẩu. */
    public boolean updateProfile(int id, String fullname, String phone, String newPassword) {
        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("phone", phone);
        if (!newPassword.isEmpty()) {
            values.put("password", PasswordHasher.hash(newPassword));
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update("users", values, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private static User map(Cursor c) {
        return new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), Role.valueOf(c.getString(4)));
    }
}

