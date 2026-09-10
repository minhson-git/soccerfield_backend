package com.ms.test_api.mapper;

import org.springframework.stereotype.Component;

import com.ms.test_api.dto.request.FieldRequest;
import com.ms.test_api.dto.response.FieldResponse;
import com.ms.test_api.entity.Field;
import com.ms.test_api.entity.enums.FieldStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FieldMapper {

    private final BranchMapper branchMapper;

    public FieldResponse toResponse(Field field) {
        if (field == null) {
            return null;
        }
        return new FieldResponse(
                field.getId(),
                field.getName(),
                field.getFieldType(),
                field.getBasePrice(),
                field.getStatus(),
                branchMapper.toResponse(field.getBranch()));
    }

    public void applyRequest(Field field, FieldRequest request) {
        field.setName(request.name());
        field.setFieldType(request.fieldType());
        field.setBasePrice(request.basePrice());
        field.setStatus(request.status() != null ? request.status() : FieldStatus.ACTIVE);
    }
}