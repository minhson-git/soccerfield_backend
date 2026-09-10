package com.ms.test_api.dto.request;

import java.math.BigDecimal;

import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.entity.enums.FieldType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FieldRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotNull
        FieldType fieldType,

        @NotNull @Positive
        BigDecimal basePrice,

        FieldStatus status,

        @NotNull
        Long branchId
) {}