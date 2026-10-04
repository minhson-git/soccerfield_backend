package com.ms.test_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record PriceQuoteResponse(
        Long fieldId,
        LocalDate date,
        @JsonFormat (pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        long durationMinutes,
        BigDecimal totalPrice)
{}