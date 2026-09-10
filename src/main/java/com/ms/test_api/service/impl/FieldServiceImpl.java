package com.ms.test_api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.dto.request.FieldRequest;
import com.ms.test_api.dto.response.FieldResponse;
import com.ms.test_api.entity.Branch;
import com.ms.test_api.entity.Field;
import com.ms.test_api.exception.ResourceNotFoundException;
import com.ms.test_api.mapper.FieldMapper;
import com.ms.test_api.repository.BranchRepository;
import com.ms.test_api.repository.FieldRepository;
import com.ms.test_api.repository.specification.FieldFilter;
import com.ms.test_api.repository.specification.FieldSpecification;
import com.ms.test_api.service.FieldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService {

    private final FieldRepository fieldRepository;
    private final BranchRepository branchRepository;
    private final FieldMapper fieldMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<FieldResponse> searchFields(FieldFilter filter, Pageable pageable) {
        return fieldRepository.findAll(FieldFilter.filter(filter), pageable)
                .map(fieldMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FieldResponse getFieldById(Long id) {
        return fieldMapper.toResponse(findField(id));
    }

    @Override
    @Transactional
    public FieldResponse createField(FieldRequest request) {
        Field field = new Field();
        fieldMapper.applyRequest(field, request);
        field.setBranch(findBranch(request.branchId()));
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional
    public FieldResponse updateField(Long id, FieldRequest request) {
        Field field = findField(id);
        fieldMapper.applyRequest(field, request);
        field.setBranch(findBranch(request.branchId()));
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional
    public void deleteField(Long id) {
        fieldRepository.delete(findField(id));
    }

    private Field findField(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + id));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }
}