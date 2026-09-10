package com.ms.test_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ms.test_api.entity.enums.BookingStatus;

public record BookingResponse(
        Long id,
        LocalDate bookingDate,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        BookingStatus status,
        BigDecimal totalPrice,
        UserSummaryResponse user,
        FieldResponse field
) {}