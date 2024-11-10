package com.ms.test_api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.specification.FieldSpecification;
import com.ms.test_api.exception.BranchNotFoundException;
import com.ms.test_api.exception.FieldNotFoundException;
import com.ms.test_api.modal.Branch;
import com.ms.test_api.modal.Field;
import com.ms.test_api.reponsitory.BranchReponsitory;
import com.ms.test_api.reponsitory.FieldRepository;
import com.ms.test_api.service.FieldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService{

    private final FieldRepository fieldRepository;

    private final BranchReponsitory branchReponsitory;

    @Override
    public Page<FieldDTO> getAllFields(int page, int size, String branchName, String fieldType, Boolean status) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Field> spec = FieldSpecification.filterFields(branchName, fieldType, status);
        
        Page<Field> fieldPage = fieldRepository.findAll(spec, pageable);

        return fieldPage.map(field -> new FieldDTO(
                            field.getFieldId(),
                            field.getFieldType(),
                            field.getPricePerHour(),
                            field.isStatus(),
                            new BranchDTO(
                                field.getBranch().getBranchId(),
                                field.getBranch().getBranchName(),
                                field.getBranch().getAddress(),
                                field.getBranch().getPhone())
                            ));
    }

    @Override
    public Field addField(Field field) {
        return fieldRepository.save(field);
    }

    @Override
    public ResponseEntity<ApiResponse<FieldDTO>> getFieldById(int id) {
        try {
            Field field = fieldRepository.findByFieldId(id)
                    .orElseThrow(() -> new FieldNotFoundException("Field not exist with id: " + id));
            FieldDTO fieldDTO = new FieldDTO(
                field.getFieldId(),
                field.getFieldType(),
                field.getPricePerHour(),
                field.isStatus(),
                new BranchDTO(
                    field.getBranch().getBranchId(),
                    field.getBranch().getBranchName(),
                    field.getBranch().getAddress(),
                    field.getBranch().getPhone()
                )
            );
            ApiResponse<FieldDTO> response = new ApiResponse<FieldDTO>(
                "Successfully retrieved field data", 
                HttpStatus.OK.value(), 
                fieldDTO
            );
            return new ResponseEntity<ApiResponse<FieldDTO>>(response, HttpStatus.OK);
        } catch (FieldNotFoundException ex) {
            ApiResponse<FieldDTO> response = new ApiResponse<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                null
            );
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<FieldDTO> response = new ApiResponse<>(
                "Failed to retrieve branch data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Field>> updateFieldById(int id, Field fieldDetail) {
        try {
            Field field = fieldRepository.findByFieldId(id)
                .orElseThrow(()-> new FieldNotFoundException("Field not exist with id: " + id));

            field.setFieldId(field.getFieldId());
            field.setFieldType(fieldDetail.getFieldType());
            field.setPricePerHour(fieldDetail.getPricePerHour());
            field.setStatus(fieldDetail.isStatus());

            Branch branch = branchReponsitory.findById(field.getBranch().getBranchId()).orElseThrow(() -> new BranchNotFoundException("Branch not exist with id: "+ id));
            field.setBranch(branch);

            fieldRepository.save(field);
            ApiResponse<Field> response = new ApiResponse<Field>(
                "Updated successfully field", 
                HttpStatus.OK.value(), 
                null
            );
            return new ResponseEntity<ApiResponse<Field>>(response, HttpStatus.OK);

        } catch (FieldNotFoundException ex) {
            ApiResponse<Field> response = new ApiResponse<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                null
            );
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<Field> response = new ApiResponse<>(
                "Failed to update field data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    public ResponseEntity<?> deleteField(int id) {
        try {
            Field field = fieldRepository.findByFieldId(id)
            .orElseThrow(() -> new FieldNotFoundException("Field not exist with id: " + id));

            fieldRepository.delete(field);
            ApiResponse<String> response = new ApiResponse<String>(
                "Deleted successfully field", 
                HttpStatus.OK.value(), 
                null
            ); 
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<String>(
                "Failed to delete user", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<ApiResponse<?>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
