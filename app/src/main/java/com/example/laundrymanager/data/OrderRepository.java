package com.example.laundrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.OrderStatus;
import com.example.laundrymanager.model.PaymentStatus;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.model.Stats;

import java.util.ArrayList;
import java.util.List;

public final class OrderRepository {
    private static final String SELECT = "SELECT o.id, o.weight, o.total, o.note, o.status, o.payment_status, "
            + "o.created_at, c.id, c.name, c.phone, c.address, s.id, s.name, s.price "
            + "FROM orders o "
            + "JOIN customers c ON c.id = o.customer_id "
            + "JOIN services s ON s.id = o.service_id";

    private final DBHelper helper;

    public OrderRepository(Context context) {
        helper = DBHelper.getInstance(context);
    }

    /** Toàn bộ đơn giặt, mới nhất trước. */
    public List<Order> findAll() {
        List<Order> result = new ArrayList<>();
        try (Cursor c = helper.getReadableDatabase().rawQuery(SELECT + " ORDER BY o.id DESC", null)) {
            while (c.moveToNext()) {
                result.add(map(c));
            }
        }
        return result;
    }

    public Order find(int id) {
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                SELECT + " WHERE o.id = ?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? map(c) : null;
        }
    }

    /** Thêm mới nếu {@code id == 0}, ngược lại cập nhật. */
    public boolean save(Order order) {
        ContentValues values = new ContentValues();
        values.put("customer_id", order.customer().id());
        values.put("service_id", order.service().id());
        values.put("weight", order.weight());
        values.put("total", order.total());
        values.put("note", order.note());
        values.put("status", order.status().name());
        values.put("payment_status", order.payment().name());
        values.put("created_at", order.createdAt());
        SQLiteDatabase db = helper.getWritableDatabase();
        if (order.id() == 0) {
            return db.insert("orders", null, values) != -1;
        }
        return db.update("orders", values, "id = ?", new String[]{String.valueOf(order.id())}) > 0;
    }

    public boolean delete(int id) {
        return helper.getWritableDatabase().delete("orders", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Stats stats() {
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*), COALESCE(SUM(total), 0), "
                        + "COALESCE(SUM(CASE WHEN payment_status = ? THEN total END), 0), "
                        + "(SELECT COUNT(*) FROM customers), (SELECT COUNT(*) FROM services) FROM orders",
                new String[]{PaymentStatus.PAID.name()})) {
            c.moveToFirst();
            return new Stats(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getInt(3), c.getInt(4));
        }
    }

    private static Order map(Cursor c) {
        Customer customer = new Customer(c.getInt(7), c.getString(8), c.getString(9), c.getString(10));
        Service service = new Service(c.getInt(11), c.getString(12), c.getDouble(13));
        return new Order(c.getInt(0), customer, service, c.getDouble(1), c.getDouble(2), c.getString(3),
                OrderStatus.valueOf(c.getString(4)), PaymentStatus.valueOf(c.getString(5)), c.getLong(6));
    }
}

