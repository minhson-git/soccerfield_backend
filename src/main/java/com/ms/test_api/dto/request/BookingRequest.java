package com.ms.test_api.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(

        @NotNull
        Long fieldId,

        @NotNull
        @FutureOrPresent(message = "Booking date cannot be in the past")
        LocalDate bookingDate,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {}