package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Branch;

public interface BranchService {

    List<BranchDTO> getAllBranchs();

    Branch addBranch(Branch branch);

    ResponseEntity<ApiResponse<BranchDTO>> getBranchById(int id);

    ResponseEntity<ApiResponse<BranchDTO>> updateBranch(int id, Branch branch);

    boolean deleteBranch(int id);

}
