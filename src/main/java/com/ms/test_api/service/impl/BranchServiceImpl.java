package com.ms.test_api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.dto.request.BranchRequest;
import com.ms.test_api.dto.response.BranchResponse;
import com.ms.test_api.entity.Branch;
import com.ms.test_api.exception.BadRequestException;
import com.ms.test_api.exception.ResourceNotFoundException;
import com.ms.test_api.mapper.BranchMapper;
import com.ms.test_api.repository.BranchRepository;
import com.ms.test_api.service.BranchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        return branchMapper.toResponse(findBranch(id));
    }

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        validateOpeningHours(request);
        Branch branch = new Branch();
        branchMapper.applyRequest(branch, request);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        validateOpeningHours(request);
        Branch branch = findBranch(id);
        branchMapper.applyRequest(branch, request);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        branchRepository.delete(findBranch(id));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }

    private void validateOpeningHours(BranchRequest request) {
        if (!request.closingTime().isAfter(request.openingTime())) {
            throw new BadRequestException("Closing time must be after opening time");
        }
    }
}