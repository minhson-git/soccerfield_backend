package com.ms.test_api.repository.specification;

import java.time.LocalDate;

import com.ms.test_api.entity.enums.BookingStatus;

public record BookingFilter(
        Long userId,
        String username,
        String branchName,
        BookingStatus status,
        LocalDate bookingDate
) {}