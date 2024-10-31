package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.model.Field;


public interface FieldService {

    List<FieldDTO> getAllFields();

    Field addField(Field field);

    ResponseEntity<FieldDTO> getFieldById(int id);

    ResponseEntity<Field> updateFieldById(int id, Field field);

    ResponseEntity<?> deleteField(int id);

}
