package com.example.backend.entity.enums;

public enum PaymentMethod {
    COD,
    VNPAY,
    BANK;

    public boolean isOnline() {
        return this == VNPAY || this == BANK;
    }
}
