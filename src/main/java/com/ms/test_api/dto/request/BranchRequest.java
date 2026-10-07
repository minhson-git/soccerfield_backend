package com.ms.test_api.dto.request;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BranchRequest(

        @NotBlank(message = "Branch name is required")
        @Size(max = 100, message = "Branch name must be at most 100 characters")
        String name,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        @Size(max = 50, message = "District must be at most 50 characters")
        String district,

        @Size(max = 20, message = "Phone number must be at most 20 characters")
        String phone,

        @NotNull(message = "Opening time is required and must be in HH:mm format")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openingTime,

        @NotNull(message = "Closing time is required and must be in HH:mm format")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closingTime,

        Long ownerId
) {}
