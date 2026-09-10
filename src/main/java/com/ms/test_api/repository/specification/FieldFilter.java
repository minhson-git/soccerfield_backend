package com.ms.test_api.repository.specification;

import java.math.BigDecimal;

import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.entity.enums.FieldType;

public record FieldFilter(
        String branchName,
        String district,
        FieldType fieldType,
        FieldStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {}