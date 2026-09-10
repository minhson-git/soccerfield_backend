package com.ms.test_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ms.test_api.dto.request.FieldRequest;
import com.ms.test_api.dto.response.FieldResponse;
import com.ms.test_api.repository.specification.FieldFilter;

public interface FieldService {

    Page<FieldResponse> searchFields(FieldFilter filter, Pageable pageable);

    FieldResponse getFieldById(Long id);

    FieldResponse createField(FieldRequest request);

    FieldResponse updateField(Long id, FieldRequest request);

    void deleteField(Long id);
}