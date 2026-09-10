package com.ms.test_api.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ms.test_api.dto.request.FieldRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.response.FieldResponse;
import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.entity.enums.FieldType;
import com.ms.test_api.repository.specification.FieldFilter;
import com.ms.test_api.service.FieldService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FieldResponse>>> searchFields(
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) FieldType fieldType,
            @RequestParam(required = false) FieldStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        FieldFilter filter = new FieldFilter(branchName, district, fieldType, status, minPrice, maxPrice);
        return ApiResponse.ok("Fields retrieved successfully", fieldService.searchFields(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FieldResponse>> getField(@PathVariable Long id) {
        return ApiResponse.ok("Field retrieved successfully", fieldService.getFieldById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<ApiResponse<FieldResponse>> createField(@RequestBody @Valid FieldRequest request) {
        return ApiResponse.created("Field created successfully", fieldService.createField(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<ApiResponse<FieldResponse>> updateField(
            @PathVariable Long id,
            @RequestBody @Valid FieldRequest request) {
        return ApiResponse.ok("Field updated successfully", fieldService.updateField(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable Long id) {
        fieldService.deleteField(id);
        return ApiResponse.ok("Field deleted successfully", null);
    }

    // TODO (Day 9): GET /{id}/availability?date=yyyy-MM-dd
}