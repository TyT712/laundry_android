package com.example.laundrymanager.model;

public record Stats(int orders, double revenue, double paid, int customers, int services) {

    public double unpaid() {
        return revenue - paid;
    }
}

