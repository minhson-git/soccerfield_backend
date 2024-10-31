package com.ms.test_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.model.Branch;
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
    public ResponseEntity<List<BranchDTO>> getAllBranch(){
        return branchServiceImpl.getAllBranchs();
    }

    @PostMapping
    public Branch createBranch(@RequestBody Branch branch) {
        return branchServiceImpl.addBranch(branch);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable int id){
        return branchServiceImpl.getBranchById(id);
    }   
    
     @PutMapping("/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable int id, @RequestBody Branch branchDetails) {
        return branchServiceImpl.updateBranch(id, branchDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable int id) {
        return branchServiceImpl.deleteBranch(id);
    }
    
}
