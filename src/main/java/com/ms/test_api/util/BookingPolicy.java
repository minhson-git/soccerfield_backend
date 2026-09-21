package com.ms.test_api.util;

public final class BookingPolicy {

    public static final int SLOT_MINUTES = 30;              // D1
    public static final int MIN_DURATION_MINUTES = 60;      // D4
    public static final int MAX_DURATION_MINUTES = 180;     // D4
    public static final int MAX_ADVANCE_DAYS = 30;          // D4
    public static final int PENDING_EXPIRY_MINUTES = 30;    // D3
    public static final int CANCEL_DEADLINE_HOURS = 2;      // D8

    private BookingPolicy() {
    }
}