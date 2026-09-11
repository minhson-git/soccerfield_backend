package com.ms.test_api.exception;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ms.test_api.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // =====================================================
    // Exception nghiệp vụ
    // =====================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookingConflict(
            ConflictException ex, HttpServletRequest request) {
        return ApiResponse.error(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // =====================================================
    // Spring Security (tầng method security)
    // =====================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource", request.getRequestURI());
    }

    // =====================================================
    // Validation trên @RequestParam / @PathVariable
    // =====================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> Optional.ofNullable(violation.getMessage()).orElse("Invalid value"),
                        (first, second) -> first,
                        LinkedHashMap::new));

        return ResponseEntity.badRequest().body(ApiResponse.of(
                "Validation failed", HttpStatus.BAD_REQUEST, request.getRequestURI(), violations));
    }

    // =====================================================
    // Ràng buộc ở tầng database
    // =====================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Data integrity violation on {} {}", request.getMethod(), request.getRequestURI(), ex);

        return ApiResponse.error(HttpStatus.CONFLICT,
                "Request conflicts with existing data", request.getRequestURI());
    }

    // =====================================================
    // Lưới an toàn cuối cùng
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);

        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", request.getRequestURI());
    }

    // =====================================================
    // Override của ResponseEntityExceptionHandler
    // =====================================================

    /** @Valid trên @RequestBody thất bại — trả về map field → message. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(),
                        Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value")));

        ApiResponse<Map<String, String>> body = ApiResponse.of(
                "Validation failed", HttpStatus.BAD_REQUEST, resolvePath(request), fieldErrors);

        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /** JSON hỏng, hoặc giá trị enum không hợp lệ. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        String message = "Request body is missing or malformed";

        if (ex.getCause() instanceof InvalidFormatException cause) {
            String field = cause.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            Class<?> targetType = cause.getTargetType();

            if (targetType != null && targetType.isEnum()) {
                message = "Invalid value '%s' for field '%s'. Allowed values: %s".formatted(
                        cause.getValue(), field, Arrays.toString(targetType.getEnumConstants()));
            } else {
                message = "Invalid value '%s' for field '%s'".formatted(cause.getValue(), field);
            }
        }

        ApiResponse<Void> body = ApiResponse.of(
                message, HttpStatus.BAD_REQUEST, resolvePath(request), null);

        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /** Sai kiểu ở @PathVariable / @RequestParam, ví dụ /bookings/abc. */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        String name = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : Optional.ofNullable(ex.getPropertyName()).orElse("parameter");

        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "the expected type";

        String message = "Parameter '%s' has invalid value '%s', expected %s".formatted(
                name, ex.getValue(), requiredType);

        ApiResponse<Void> body = ApiResponse.of(
                message, HttpStatus.BAD_REQUEST, resolvePath(request), null);

        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /** Tất cả exception chuẩn còn lại của Spring MVC — thay body bằng ApiResponse. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {

        HttpStatus status = HttpStatus.valueOf(statusCode.value());

        if (status.is5xxServerError()) {
            log.error("Spring MVC exception on {}", resolvePath(request), ex);
        }

        ApiResponse<Void> apiResponse = ApiResponse.of(
                friendlyMessage(ex, status), status, resolvePath(request), null);

        return new ResponseEntity<>(apiResponse, headers, statusCode);
    }

    // =====================================================
    // Helper
    // =====================================================

    private String friendlyMessage(Exception ex, HttpStatus status) {
        if (status.is5xxServerError()) {
            return "Internal server error";
        }
        return switch (status) {
            case NOT_FOUND -> "Endpoint not found";
            case METHOD_NOT_ALLOWED -> "HTTP method is not supported for this endpoint";
            case UNSUPPORTED_MEDIA_TYPE -> "Content type is not supported";
            default -> Optional.ofNullable(ex.getMessage()).orElse(status.getReasonPhrase());
        };
    }

    private String resolvePath(WebRequest request) {
        return request instanceof ServletWebRequest servletWebRequest
                ? servletWebRequest.getRequest().getRequestURI()
                : null;
    }
}