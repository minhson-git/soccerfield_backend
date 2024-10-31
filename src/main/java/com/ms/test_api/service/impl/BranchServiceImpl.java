package com.ms.test_api.service.impl;

import java.util.List;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.model.Branch;
import com.ms.test_api.reponsitory.BranchReponsitory;
import com.ms.test_api.service.BranchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService{

    private final BranchReponsitory branchReponsitory;

    @Override
    public ResponseEntity<List<BranchDTO>> getAllBranchs() {
        List<Branch> branchs = branchReponsitory.findAll();
        List<BranchDTO> branchDTOs = branchs.stream()
            .map(branch -> new BranchDTO(
                branch.getBranchId(),
                branch.getBranchName(),
                branch.getAddress(),
                branch.getPhone()))
            .toList();
        return ResponseEntity.ok(branchDTOs);
    }

    @Override
    public Branch addBranch(Branch branch) {
        return branchReponsitory.save(branch);
    }

    @Override
    public ResponseEntity<BranchDTO> getBranchById(int id) {
        Branch branchs = branchReponsitory.findById(id).orElseThrow(() -> new ResourceNotFoundException("Branch not exist with id: " + id));
        BranchDTO branchDTOs = new BranchDTO(branchs.getBranchId(), branchs.getBranchName(), branchs.getAddress(), branchs.getPhone());
        return ResponseEntity.ok(branchDTOs);
    }

    @Override
    public ResponseEntity<Branch> updateBranch(int id, Branch branchDetails) {
        Branch branch = branchReponsitory.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not exist with id: " + id));

        branch.setBranchName(branchDetails.getBranchName());
        branch.setAddress(branchDetails.getAddress());
        branch.setPhone(branchDetails.getPhone());

        Branch updatedBranch = branchReponsitory.save(branch);
        return ResponseEntity.ok(updatedBranch);
    }

    @Override
    public ResponseEntity<?> deleteBranch(int id) {
        Branch branch = branchReponsitory.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not exist with id: " + id));

        branchReponsitory.delete(branch);
        return ResponseEntity.ok().build();
    }

}
