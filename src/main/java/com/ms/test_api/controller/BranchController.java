package com.ms.test_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ms.test_api.dto.request.BranchRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.response.BranchResponse;
import com.ms.test_api.service.BranchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {
        return ApiResponse.ok("Branches retrieved successfully", branchService.getAllBranches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranch(@PathVariable Long id) {
        return ApiResponse.ok("Branch retrieved successfully", branchService.getBranchById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getOwnedBranches(Authentication authentication) {
        return ApiResponse.ok("Owned branches retrieved successfully",
                branchService.getOwnedBranches(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@RequestBody @Valid BranchRequest request) {
        return ApiResponse.created("Branch created successfully", branchService.createBranch(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @RequestBody @Valid BranchRequest request) {
        return ApiResponse.ok("Branch updated successfully", branchService.updateBranch(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ApiResponse.ok("Branch deleted successfully", null);
    }
}