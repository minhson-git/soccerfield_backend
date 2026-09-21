package com.ms.test_api.dto.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String message,
        int statusCode,
        LocalDateTime timestamp,
        String path,
        T data) {

    public static <T> ApiResponse<T> of(String message, HttpStatus status, String path, T data) {
        return new ApiResponse<>(message, status.value(), LocalDateTime.now(), path, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(of(message, HttpStatus.OK, null, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(of(message, HttpStatus.CREATED, null, data));
    }

    public static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(of(message, status, path, null));
    }
}