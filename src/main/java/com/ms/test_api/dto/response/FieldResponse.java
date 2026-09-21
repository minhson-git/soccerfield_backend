package com.ms.test_api.dto.response;

import java.math.BigDecimal;

import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.entity.enums.FieldType;

public record FieldResponse(
        Long id,
        String name,
        FieldType fieldType,
        BigDecimal basePrice,
        FieldStatus status,
        BranchResponse branch
) {}