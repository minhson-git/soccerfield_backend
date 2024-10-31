package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.exception.FieldNotFoundException;
import com.ms.test_api.model.Field;
import com.ms.test_api.reponsitory.FieldRepository;
import com.ms.test_api.service.FieldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService{

    private final FieldRepository fieldRepository;

    @Override
    public List<FieldDTO> getAllFields() {
        List<Field> fields = fieldRepository.findAll();
        return fields.stream().map(field -> 
            new FieldDTO(
                field.getFieldId(),
                field.getFieldType(),
                field.getPricePerHour(),
                field.getStatus(),
                new BranchDTO(
                    field.getBranch().getBranchId(),
                    field.getBranch().getBranchName(),
                    field.getBranch().getAddress(),
                    field.getBranch().getPhone()
                )
            )
        ).collect(Collectors.toList());
    }

    @Override
    public Field addField(Field field) {
        return fieldRepository.save(field);
    }

    @Override
    public ResponseEntity<FieldDTO> getFieldById(int id) {
        Field field = fieldRepository.findByFieldId(id)
                .orElseThrow(() -> new FieldNotFoundException("Field not exist with id: " + id));
        FieldDTO fieldDTO = new FieldDTO(
            field.getFieldId(),
            field.getFieldType(),
            field.getPricePerHour(),
            field.getStatus(),
            new BranchDTO(
                field.getBranch().getBranchId(),
                field.getBranch().getBranchName(),
                field.getBranch().getAddress(),
                field.getBranch().getPhone()
            )
        );
        return ResponseEntity.ok(fieldDTO);
    }

    @Override
    public ResponseEntity<Field> updateFieldById(int id, Field fieldDetail) {
        Field field = fieldRepository.findByFieldId(id)
                .orElseThrow(()-> new FieldNotFoundException("Field not exist with id: " + id));

        field.setFieldId(fieldDetail.getFieldId());
        field.setFieldType(fieldDetail.getFieldType());
        field.setPricePerHour(fieldDetail.getPricePerHour());
        field.setStatus(fieldDetail.getStatus());
        field.setBranch(fieldDetail.getBranch());

        Field updateField = fieldRepository.save(field);
        return ResponseEntity.ok(updateField);
    }

    @Override
    public ResponseEntity<?> deleteField(int id) {
        Field field = fieldRepository.findByFieldId(id)
                .orElseThrow(() -> new FieldNotFoundException("Field not exist with id: " + id));

        fieldRepository.delete(field);
        return ResponseEntity.ok().build();
    }
}
