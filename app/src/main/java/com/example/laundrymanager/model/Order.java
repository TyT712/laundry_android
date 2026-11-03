package com.example.laundrymanager.model;

/** Đơn giặt; {@code id == 0} nghĩa là đơn chưa được lưu. */
public record Order(
        int id,
        Customer customer,
        Service service,
        double weight,
        double total,
        String note,
        OrderStatus status,
        PaymentStatus payment,
        long createdAt) {
}

