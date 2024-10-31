package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.model.Branch;

public interface BranchService {

    ResponseEntity<List<BranchDTO>> getAllBranchs();

    Branch addBranch(Branch branch);

    ResponseEntity<BranchDTO> getBranchById(int id);

    ResponseEntity<Branch> updateBranch(int id, Branch branch);

    ResponseEntity<?> deleteBranch(int id);

}
