package com.example.laundrymanager.model;

/** Dịch vụ giặt; {@code price} là đơn giá theo kg. */
public record Service(int id, String name, double price) {

    public double priceFor(double kg) {
        return price * kg;
    }
}

