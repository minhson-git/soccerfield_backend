package com.ms.test_api.dto;

import lombok.Data;

@Data
public class FieldDTO {

    private int fieldId;
    private String fieldType;
    private double pricePerHour;
    private boolean status;
    private BranchDTO branch;

    public FieldDTO(int fieldId, String fieldType, BranchDTO branchDTO) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
        this.branch = branchDTO;
    }
    
    public FieldDTO(int id, String fieldType, double pricePerHour, boolean status, BranchDTO branchDTO){
        this.fieldId = id;
        this.fieldType = fieldType;
        this.pricePerHour = pricePerHour;
        this.status = status;
        this.branch = branchDTO;
    }
}
