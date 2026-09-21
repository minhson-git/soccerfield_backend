package com.ms.test_api.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record FieldAvailabilityResponse(
        Long fieldId,
        String fieldName,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime openingTime,
        @JsonFormat(pattern = "HH:mm") LocalTime closingTime,
        int slotMinutes,
        int minDurationMinutes,
        int maxDurationMinutes,
        List<AvailabilitySlotResponse> slots
) {}