package com.example.laundrymanager.model;

import com.example.laundrymanager.R;

public enum OrderStatus implements Labeled {
    RECEIVED(R.string.status_received),
    WASHING(R.string.status_washing),
    DRYING(R.string.status_drying),
    DONE(R.string.status_done),
    DELIVERED(R.string.status_delivered);

    private final int label;

    OrderStatus(int label) {
        this.label = label;
    }

    @Override
    public int label() {
        return label;
    }
}

