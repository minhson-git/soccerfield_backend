package com.ms.test_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ms.test_api.dto.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex){
        ApiResponse<Object> response = new ApiResponse<Object>(
            "You do not have permission", 
            HttpStatus.FORBIDDEN.value(),
            null 
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOtherExceptions (Exception ex){
        ApiResponse<Object> response = new ApiResponse<Object>(
            "Internal Server Error", 
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex){
        ApiResponse<Object> response = new ApiResponse<Object>(
            ex.getMessage(), 
            HttpStatus.BAD_REQUEST.value(), 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex){
        ApiResponse<Object> response = new ApiResponse<Object>(
            ex.getFieldError().getDefaultMessage(), 
            HttpStatus.BAD_REQUEST.value(), 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
