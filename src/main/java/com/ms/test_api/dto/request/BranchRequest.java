package com.ms.test_api.dto.request;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BranchRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Size(max = 255)
        String address,

        @Size(max = 50)
        String district,

        @Size(max = 20)
        String phone,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime openingTime,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime closingTime,

        Long ownerId
) {}