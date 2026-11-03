package com.example.laundrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.example.laundrymanager.model.Service;

import java.util.ArrayList;
import java.util.List;

public final class ServiceRepository {
    private final DBHelper helper;

    public ServiceRepository(Context context) {
        helper = DBHelper.getInstance(context);
    }

    public List<Service> findAll() {
        List<Service> result = new ArrayList<>();
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, name, price FROM services ORDER BY id", null)) {
            while (c.moveToNext()) {
                result.add(new Service(c.getInt(0), c.getString(1), c.getDouble(2)));
            }
        }
        return result;
    }

    /** Thêm mới nếu {@code id == 0}, ngược lại cập nhật. @return {@code false} nếu trùng tên dịch vụ. */
    public boolean save(Service service) {
        ContentValues values = new ContentValues();
        values.put("name", service.name());
        values.put("price", service.price());
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            if (service.id() == 0) {
                return db.insert("services", null, values) != -1;
            }
            return db.update("services", values, "id = ?", new String[]{String.valueOf(service.id())}) > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    /** @return {@code false} nếu không xóa được (ví dụ dịch vụ đã được dùng trong đơn giặt). */
    public boolean delete(int id) {
        try {
            return helper.getWritableDatabase().delete("services", "id = ?", new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }
}

