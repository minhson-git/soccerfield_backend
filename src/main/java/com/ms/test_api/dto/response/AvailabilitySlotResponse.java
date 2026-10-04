package com.ms.test_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record AvailabilitySlotResponse(
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        boolean available,
        BigDecimal hourlyRate) 
{}