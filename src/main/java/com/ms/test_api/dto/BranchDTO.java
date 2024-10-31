package com.ms.test_api.dto;

import lombok.Data;

@Data
public class BranchDTO {
    private int id;
    private String branchName;
    private String address;
    private String phone;
   
    public BranchDTO(int id, String branchName, String address, String phone){
        this.id = id;
        this.branchName = branchName;
        this.address = address;
        this.phone = phone;
    }
}
