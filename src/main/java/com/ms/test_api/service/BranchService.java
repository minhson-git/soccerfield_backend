package com.ms.test_api.service;

import java.util.List;

import com.ms.test_api.dto.request.BranchRequest;
import com.ms.test_api.dto.response.BranchResponse;

public interface BranchService {

    List<BranchResponse> getAllBranches();

    BranchResponse getBranchById(Long id);

    BranchResponse createBranch(BranchRequest request);

    BranchResponse updateBranch(Long id, BranchRequest request);

    void deleteBranch(Long id);

    List<BranchResponse> getOwnedBranches(String username);
}