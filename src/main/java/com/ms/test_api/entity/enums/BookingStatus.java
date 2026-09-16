package com.ms.test_api.entity.enums;

import java.util.Set;

public enum BookingStatus {

    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    EXPIRED
;     
public static final Set<BookingStatus> OCCUPYING = Set.of(PENDING, CONFIRMED);

}
