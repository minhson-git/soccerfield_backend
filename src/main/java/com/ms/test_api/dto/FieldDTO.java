package com.ms.test_api.dto;

import lombok.Data;

@Data
public class FieldDTO {

    private int fieldId;
    private String fieldType;
    private double pricePerHour;
    private String status;
    private BranchDTO branch;

    public FieldDTO(int fieldId, String fieldType) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
    }
    
    public FieldDTO(int id, String fieldType, double pricePerHour, String status, BranchDTO branchDTO){
        this.fieldId = id;
        this.fieldType = fieldType;
        this.pricePerHour = pricePerHour;
        this.status = status;
        this.branch = branchDTO;
    }
}
