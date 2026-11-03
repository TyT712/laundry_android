package com.example.laundrymanager.model;

import com.example.laundrymanager.R;

public enum PaymentStatus implements Labeled {
    UNPAID(R.string.payment_unpaid),
    PAID(R.string.payment_paid);

    private final int label;

    PaymentStatus(int label) {
        this.label = label;
    }

    @Override
    public int label() {
        return label;
    }
}

