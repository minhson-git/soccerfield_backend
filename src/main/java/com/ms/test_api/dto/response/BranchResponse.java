package com.ms.test_api.dto.response;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record BranchResponse(
        Long id,
        String name,
        String address,
        String district,
        String phone,
        @JsonFormat(pattern = "HH:mm") LocalTime openingTime,
        @JsonFormat(pattern = "HH:mm") LocalTime closingTime
) {}