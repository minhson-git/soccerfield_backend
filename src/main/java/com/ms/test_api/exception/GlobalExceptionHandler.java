package com.ms.test_api.exception;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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

    /**
     * Annotation như @PreAuthorize ném "Access Denied" chung chung — thay bằng câu dễ hiểu.
     * Service tự ném với lý do cụ thể ("You can only cancel your own booking") — giữ nguyên.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        String message = ex.getMessage() == null || "Access Denied".equalsIgnoreCase(ex.getMessage())
                ? "You do not have permission to access this resource"
                : ex.getMessage();

        return ApiResponse.error(HttpStatus.FORBIDDEN, message, request.getRequestURI());
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

    /**
     * Phân loại theo thông báo gốc của MySQL. Không trả nguyên văn thông báo đó cho client
     * vì nó lộ tên bảng, tên ràng buộc và giá trị trong database.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Data integrity violation on {} {}", request.getMethod(), request.getRequestURI(), ex);

        String detail = Optional.ofNullable(NestedExceptionUtils.getMostSpecificCause(ex).getMessage())
                .orElse("")
                .toLowerCase(Locale.ROOT);

        if (detail.contains("duplicate entry")) {
            return ApiResponse.error(HttpStatus.CONFLICT,
                    "A record with the same unique value already exists", request.getRequestURI());
        }
        if (detail.contains("cannot delete or update a parent row")) {
            return ApiResponse.error(HttpStatus.CONFLICT,
                    "This record is still used by other data and cannot be deleted. "
                            + "Remove or reassign the related records first",
                    request.getRequestURI());
        }
        if (detail.contains("cannot add or update a child row")) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST,
                    "A referenced record does not exist", request.getRequestURI());
        }
        if (detail.contains("cannot be null")) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST,
                    "A required value is missing", request.getRequestURI());
        }
        if (detail.contains("data too long") || detail.contains("out of range")) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST,
                    "A value is too long or out of the allowed range", request.getRequestURI());
        }

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

    /** Body thiếu, JSON sai cú pháp, hoặc một trường sai kiểu / sai định dạng. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Throwable cause = ex.getCause();
        String message;

        if (cause instanceof InvalidFormatException invalid) {
            // Ví dụ: "startTime": "25:00", "fieldType": "FUTSAL", "basePrice": "abc"
            message = "Invalid value '%s' for field '%s', expected %s".formatted(
                    invalid.getValue(), jsonPath(invalid), describeExpected(invalid.getTargetType()));
        } else if (cause instanceof MismatchedInputException mismatched && !jsonPath(mismatched).isEmpty()) {
            // Ví dụ: "fieldId": {"id": 1} hoặc "startTime": 1800
            message = "Field '%s' has the wrong type, expected %s".formatted(
                    jsonPath(mismatched), describeExpected(mismatched.getTargetType()));
        } else if (cause instanceof JsonParseException) {
            message = "Request body is not valid JSON";
        } else if (ex.getMessage() != null && ex.getMessage().startsWith("Required request body is missing")) {
            message = "Request body is required";
        } else {
            message = "Request body is missing or malformed";
        }

        ApiResponse<Void> body = ApiResponse.of(
                message, HttpStatus.BAD_REQUEST, resolvePath(request), null);

        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /** Thiếu @RequestParam bắt buộc, ví dụ gọi /fields/1/availability mà không có ?date=. */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        String message = "Missing required parameter '%s'".formatted(ex.getParameterName());

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

        String message = "Parameter '%s' has invalid value '%s', expected %s".formatted(
                name, ex.getValue(), describeExpected(ex.getRequiredType()));

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

    @ExceptionHandler({ PessimisticLockingFailureException.class, CannotAcquireLockException.class })
    public ResponseEntity<ApiResponse<Void>> handleLockFailure(
            Exception ex, HttpServletRequest request) {

        log.warn("Lock acquisition failed on {} {}", request.getMethod(), request.getRequestURI(), ex);

        // Khoá được dùng cả khi đặt sân lẫn khi xác nhận / từ chối / huỷ,
        // nên thông báo không được giả định là đang đặt sân.
        return ApiResponse.error(HttpStatus.CONFLICT,
                "Another request is updating the same data right now, please try again in a moment",
                request.getRequestURI());
    }

    // =====================================================
    // Helper
    // =====================================================

    private static String jsonPath(JsonMappingException ex) {
        return ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
    }

    /** Mô tả kiểu mong đợi theo cách client đọc hiểu được, thay vì tên lớp Java. */
    private static String describeExpected(Class<?> type) {
        if (type == null) {
            return "a valid value";
        }
        if (type.isEnum()) {
            return "one of " + Arrays.toString(type.getEnumConstants());
        }
        if (type == LocalDate.class) {
            return "a date in yyyy-MM-dd format";
        }
        if (type == LocalTime.class) {
            return "a time in HH:mm format";
        }
        if (type == LocalDateTime.class) {
            return "a date-time in yyyy-MM-ddTHH:mm:ss format";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "true or false";
        }
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            return "a number";
        }
        if (type == String.class) {
            return "a text value";
        }
        return "a valid " + type.getSimpleName();
    }

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