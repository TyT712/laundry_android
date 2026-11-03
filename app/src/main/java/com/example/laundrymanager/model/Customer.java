package com.example.laundrymanager.model;

/** {@code id == 0} nghĩa là khách hàng chưa được lưu. */
public record Customer(int id, String name, String phone, String address) {
}

