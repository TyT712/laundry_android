package com.example.laundrymanager.model;

public record Order(
        int id,
        Customer customer,
        Service service,
        double weight,
        double total,
        String note,
        OrderStatus status,
        PaymentStatus payment,
        long createdAt,
        long dueDate
) {
    /** Kiểm tra xem đơn hàng đã quá hạn hẹn trả hay chưa (chỉ tính nếu chưa giao). */
    public boolean isOverdue() {
        return dueDate > 0 && System.currentTimeMillis() > dueDate && status != OrderStatus.DELIVERED;
    }
}
