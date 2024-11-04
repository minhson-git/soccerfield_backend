package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Branch;
import com.ms.test_api.reponsitory.BranchReponsitory;
import com.ms.test_api.service.BranchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService{

    private final BranchReponsitory branchReponsitory;

    @Override
    public List<BranchDTO> getAllBranchs() {
        List<Branch> branchs = branchReponsitory.findAll();
        return branchs.stream()
            .map(branch -> new BranchDTO(
                branch.getBranchId(),
                branch.getBranchName(),
                branch.getAddress(),
                branch.getPhone()))
            .collect(Collectors.toList());
    }

    @Override
    public Branch addBranch(Branch branch) {
        return branchReponsitory.save(branch);
    }

    @Override
    public ResponseEntity<ApiResponse<BranchDTO>> getBranchById(int id) {
        try {
            Branch branch = branchReponsitory.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not exist with id: " + id));
            
            BranchDTO branchDTO = new BranchDTO(branch.getBranchId(), branch.getBranchName(), branch.getAddress(), branch.getPhone());
            
            ApiResponse<BranchDTO> response = new ApiResponse<>(
                "Successfully retrieved branch data",
                HttpStatus.OK.value(),
                branchDTO
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (ResourceNotFoundException ex) {
            ApiResponse<BranchDTO> response = new ApiResponse<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                null
            );
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<BranchDTO> response = new ApiResponse<>(
                "Failed to retrieve branch data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(int id, Branch branchDetails) {
        try {
            Branch branch = branchReponsitory.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not exist with id: " + id));
            
            branch.setBranchName(branchDetails.getBranchName());
            branch.setAddress(branchDetails.getAddress());
            branch.setPhone(branchDetails.getPhone());
    
            Branch updatedBranch = branchReponsitory.save(branch);
            
            BranchDTO branchDTO = new BranchDTO(
                updatedBranch.getBranchId(),
                updatedBranch.getBranchName(),
                updatedBranch.getAddress(),
                updatedBranch.getPhone()
            );
    
            ApiResponse<BranchDTO> response = new ApiResponse<>(
                "Successfully updated branch data",
                HttpStatus.OK.value(),
                branchDTO
            );
    
            return new ResponseEntity<>(response, HttpStatus.OK);
    
        } catch (ResourceNotFoundException ex) {
            ApiResponse<BranchDTO> response = new ApiResponse<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                null
            );
    
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<BranchDTO> response = new ApiResponse<>(
                "Failed to update branch data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
    
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    @Override
    public boolean deleteBranch(int id) {
        Branch branch = branchReponsitory.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not exist with id: " + id));

        branchReponsitory.delete(branch);
        return true;
    }

}
