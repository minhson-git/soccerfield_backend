package com.ms.test_api.exception;

import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ms.test_api.dto.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException (AccessDeniedException ex){
        ApiResponse<Object> response = new ApiResponse<Object>(
            "Access Denied", 
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

}
