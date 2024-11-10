package com.ms.test_api.service;


import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Field;


public interface FieldService {

    Page<FieldDTO> getAllFields(int page, int size, String branchName, String fieldType, Boolean status);

    Field addField(Field field);

    ResponseEntity<ApiResponse<FieldDTO>> getFieldById(int id);

    ResponseEntity<ApiResponse<Field>> updateFieldById(int id, Field field);

    ResponseEntity<?> deleteField(int id);

}
