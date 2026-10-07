package com.ms.test_api.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(

        @NotNull(message = "Field ID is required")
        Long fieldId,

        @NotNull(message = "Booking date is required")
        @FutureOrPresent(message = "Booking date cannot be in the past")
        LocalDate bookingDate,

        @NotNull(message = "Start time is required and must be in HH:mm format")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "End time is required and must be in HH:mm format")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {}