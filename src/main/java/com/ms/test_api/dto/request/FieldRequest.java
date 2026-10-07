package com.ms.test_api.dto.request;

import java.math.BigDecimal;

import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.entity.enums.FieldType;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FieldRequest(

        @NotBlank(message = "Field name is required")
        @Size(max = 100, message = "Field name must be at most 100 characters")
        String name,

        @NotNull(message = "Field type is required")
        FieldType fieldType,

        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be greater than 0")
        @Digits(integer = 10, fraction = 2,
                message = "Base price must have at most 10 integer digits and 2 decimal places")
        BigDecimal basePrice,

        FieldStatus status,

        @NotNull(message = "Branch ID is required")
        Long branchId
) {}
