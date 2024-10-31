package com.ms.test_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.model.Field;
import com.ms.test_api.service.impl.FieldServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldServiceImpl fieldServiceImpl;

    @GetMapping
    public ResponseEntity<List<FieldDTO>> getAllFields(){
        List<FieldDTO> fieldDTOs = fieldServiceImpl.getAllFields();
        return ResponseEntity.ok(fieldDTOs);
    }

    @PostMapping
    public Field addField(@RequestBody Field field){
        return fieldServiceImpl.addField(field);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldDTO> getFieldById(@PathVariable int id){
        return fieldServiceImpl.getFieldById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Field> updateField(@PathVariable int id, @RequestBody Field field){
        return fieldServiceImpl.updateFieldById(id, field);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteField(@PathVariable int id){
        return fieldServiceImpl.deleteField(id);
    }

}
