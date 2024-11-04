package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Field;


public interface FieldService {

    List<FieldDTO> getAllFields();

    Field addField(Field field);

    ResponseEntity<ApiResponse<FieldDTO>> getFieldById(int id);

    ResponseEntity<ApiResponse<Field>> updateFieldById(int id, Field field);

    ResponseEntity<?> deleteField(int id);

}
