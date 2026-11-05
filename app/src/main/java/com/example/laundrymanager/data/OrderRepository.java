package com.example.laundrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.laundrymanager.model.Customer;
import com.example.laundrymanager.model.DetailedStats;
import com.example.laundrymanager.model.Order;
import com.example.laundrymanager.model.OrderStatus;
import com.example.laundrymanager.model.PaymentStatus;
import com.example.laundrymanager.model.Service;
import com.example.laundrymanager.model.Stats;

import java.util.ArrayList;
import java.util.List;

public final class OrderRepository {
    private static final String SELECT = "SELECT o.id, o.weight, o.total, o.note, o.status, o.payment_status, "
            + "o.created_at, c.id, c.name, c.phone, c.address, s.id, s.name, s.price, o.due_date "
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

    /** Lấy danh sách các đơn hàng của một khách hàng cụ thể. */
    public List<Order> findByCustomer(int customerId) {
        List<Order> result = new ArrayList<>();
        try (Cursor c = helper.getReadableDatabase().rawQuery(
                SELECT + " WHERE o.customer_id = ? ORDER BY o.id DESC", new String[]{String.valueOf(customerId)})) {
            while (c.moveToNext()) {
                result.add(map(c));
            }
        }
        return result;
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
        values.put("due_date", order.dueDate());
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

    /** Báo cáo thống kê chi tiết có lọc theo khoảng thời gian (fromMillis -> toMillis). */
    public DetailedStats detailedStats(long fromMillis, long toMillis) {
        boolean hasRange = fromMillis > 0 && toMillis > 0;
        String whereClause = hasRange ? " WHERE created_at >= ? AND created_at <= ?" : "";
        String[] params = hasRange
                ? new String[]{PaymentStatus.PAID.name(), String.valueOf(fromMillis), String.valueOf(toMillis)}
                : new String[]{PaymentStatus.PAID.name()};

        int orders = 0;
        double revenue = 0;
        double paid = 0;
        double totalWeight = 0;
        int customersCount = 0;
        int servicesCount = 0;

        String query = "SELECT COUNT(*), COALESCE(SUM(total), 0), "
                + "COALESCE(SUM(CASE WHEN payment_status = ? THEN total END), 0), "
                + "COALESCE(SUM(weight), 0), "
                + "(SELECT COUNT(*) FROM customers), (SELECT COUNT(*) FROM services) "
                + "FROM orders" + whereClause;

        try (Cursor c = helper.getReadableDatabase().rawQuery(query, params)) {
            if (c.moveToFirst()) {
                orders = c.getInt(0);
                revenue = c.getDouble(1);
                paid = c.getDouble(2);
                totalWeight = c.getDouble(3);
                customersCount = c.getInt(4);
                servicesCount = c.getInt(5);
            }
        }

        List<DetailedStats.ServiceStat> breakdown = new ArrayList<>();
        String breakdownQuery = "SELECT s.id, s.name, COUNT(o.id), COALESCE(SUM(o.weight), 0), COALESCE(SUM(o.total), 0) "
                + "FROM orders o JOIN services s ON s.id = o.service_id "
                + (hasRange ? "WHERE o.created_at >= ? AND o.created_at <= ? " : "")
                + "GROUP BY s.id, s.name ORDER BY SUM(o.total) DESC";

        String[] breakdownParams = hasRange
                ? new String[]{String.valueOf(fromMillis), String.valueOf(toMillis)}
                : null;

        try (Cursor c = helper.getReadableDatabase().rawQuery(breakdownQuery, breakdownParams)) {
            while (c.moveToNext()) {
                breakdown.add(new DetailedStats.ServiceStat(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getDouble(3),
                        c.getDouble(4)
                ));
            }
        }

        return new DetailedStats(orders, revenue, paid, totalWeight, customersCount, servicesCount, breakdown);
    }

    private static Order map(Cursor c) {
        Customer customer = new Customer(c.getInt(7), c.getString(8), c.getString(9), c.getString(10));
        Service service = new Service(c.getInt(11), c.getString(12), c.getDouble(13));
        long createdAt = c.getLong(6);
        long dueDate = c.getLong(14);
        if (dueDate == 0) {
            dueDate = createdAt + 24 * 3600 * 1000L;
        }
        return new Order(c.getInt(0), customer, service, c.getDouble(1), c.getDouble(2), c.getString(3),
                OrderStatus.valueOf(c.getString(4)), PaymentStatus.valueOf(c.getString(5)), createdAt, dueDate);
    }
}
