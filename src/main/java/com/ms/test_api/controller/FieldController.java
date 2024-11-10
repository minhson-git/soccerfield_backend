package com.ms.test_api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Field;
import com.ms.test_api.service.impl.FieldServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldServiceImpl fieldServiceImpl;

    @GetMapping
    public ResponseEntity<Page<FieldDTO>> getAllFields(@RequestParam(required = false, defaultValue = "0") int page, 
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String branchName,
            @RequestParam(required = false, defaultValue = "") String fieldType,
            @RequestParam(required = false, defaultValue = "") Boolean status) {
        try {
            Page<FieldDTO> fieldDTOs = fieldServiceImpl.getAllFields(page, size, branchName, fieldType, status);
            ApiResponse<Page<FieldDTO>> response = new ApiResponse<>(
                "Successfully retrieved field data",
                HttpStatus.OK.value(),
                fieldDTOs
            );
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            // TODO: handle exception
            ApiResponse<Page<FieldDTO>> response = new ApiResponse<>(
                "Failed to retrieve field data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Field>> addField(@RequestBody Field field){
        try {
            fieldServiceImpl.addField(field);
            ApiResponse<Field> response = new ApiResponse<Field>(
                "Field created successfully", 
                HttpStatus.CREATED.value(), 
                null
            ); 
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Field> response = new ApiResponse<>(
                "Failed to create field",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FieldDTO>> getFieldById(@PathVariable int id){
        return fieldServiceImpl.getFieldById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Field>> updateField(@PathVariable int id, @RequestBody Field field){
        return fieldServiceImpl.updateFieldById(id, field);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteField(@PathVariable int id){
        return fieldServiceImpl.deleteField(id);
    }

}
