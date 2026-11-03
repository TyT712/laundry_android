package com.example.laundrymanager.model;

import com.example.laundrymanager.R;

public enum Role implements Labeled {
    MANAGER(R.string.role_manager),
    STAFF(R.string.role_staff);

    private final int label;

    Role(int label) {
        this.label = label;
    }

    @Override
    public int label() {
        return label;
    }
}

