package com.example.laundrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.example.laundrymanager.model.Customer;

import java.util.ArrayList;
import java.util.List;

public final class CustomerRepository {
    private final DBHelper helper;

    public CustomerRepository(Context context) {
        helper = DBHelper.getInstance(context);
    }

    /** Tìm theo tên, số điện thoại hoặc địa chỉ; từ khóa rỗng trả về tất cả. */
    public List<Customer> search(String keyword) {
        String like = "%" + keyword.trim() + "%";
        List<Customer> result = new ArrayList<>();
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, name, phone, address FROM customers "
                        + "WHERE name LIKE ? OR phone LIKE ? OR address LIKE ? ORDER BY id DESC",
                new String[]{like, like, like})) {
            while (c.moveToNext()) {
                result.add(new Customer(c.getInt(0), c.getString(1), c.getString(2), c.getString(3)));
            }
        }
        return result;
    }

    public List<Customer> findAll() {
        return search("");
    }

    /** Thêm mới nếu {@code id == 0}, ngược lại cập nhật. @return ID khách hàng (> 0) nếu thành công, -1 nếu thất bại. */
    public int save(Customer customer) {
        ContentValues values = new ContentValues();
        values.put("name", customer.name());
        values.put("phone", customer.phone());
        values.put("address", customer.address());
        SQLiteDatabase db = helper.getWritableDatabase();
        if (customer.id() == 0) {
            long newId = db.insert("customers", null, values);
            return (int) newId;
        }
        boolean ok = db.update("customers", values, "id = ?", new String[]{String.valueOf(customer.id())}) > 0;
        return ok ? customer.id() : -1;
    }

    /** @return {@code false} nếu không xóa được (ví dụ khách hàng đã có đơn giặt). */
    public boolean delete(int id) {
        try {
            return helper.getWritableDatabase().delete("customers", "id = ?", new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }
}

