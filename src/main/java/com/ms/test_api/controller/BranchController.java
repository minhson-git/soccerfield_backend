package com.ms.test_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Branch;
import com.ms.test_api.service.impl.BranchServiceImpl;

import lombok.RequiredArgsConstructor;

import java.util.*;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branchs")
public class BranchController {

    private final BranchServiceImpl branchServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranch(){
        try {
            List<BranchDTO> branchs = branchServiceImpl.getAllBranchs();
            ApiResponse<List<BranchDTO>> response = new ApiResponse<>(
                "Successfully retrieved branch data",
                HttpStatus.OK.value(),
                branchs
            );
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            // TODO: handle exception
            ApiResponse<List<BranchDTO>> response = new ApiResponse<>(
                "Failed to retrieve branch data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Branch>> createBranch(@RequestBody Branch branchDetail) {
        try {
            branchServiceImpl.addBranch(branchDetail);
            ApiResponse<Branch> response = new ApiResponse<Branch>(
                "Branch created successfully", 
                HttpStatus.CREATED.value(), 
                null
            ); 
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Branch> response = new ApiResponse<>(
                "Failed to create branch",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> getBranchById(@PathVariable int id){
        return branchServiceImpl.getBranchById(id);
    }   
    
     @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(@PathVariable int id, @RequestBody Branch branchDetails) {
        return branchServiceImpl.updateBranch(id, branchDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable int id) {
        try {
            boolean isDeleted = branchServiceImpl.deleteBranch(id);
            if (isDeleted) {
                ApiResponse<String> response = new ApiResponse<>(
                    "Branch deleted successfully",
                    HttpStatus.OK.value(),
                    null
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>(
                    "Branch not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
                );
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                "Failed to delete branch",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
