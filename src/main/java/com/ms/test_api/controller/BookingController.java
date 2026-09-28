package com.ms.test_api.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.ms.test_api.dto.request.BookingRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.response.BookingResponse;
import com.ms.test_api.entity.enums.BookingStatus;
import com.ms.test_api.repository.specification.BookingFilter;
import com.ms.test_api.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> searchBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
            Authentication authentication,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        String ownerScope = isAdmin ? null : authentication.getName();

        BookingFilter filter = new BookingFilter(
                userId, username, branchName, status, bookingDate, ownerScope);

        return ApiResponse.ok("Bookings retrieved successfully",
                bookingService.searchBookings(filter, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        BookingFilter filter = new BookingFilter(null, jwt.getSubject(), null,null, null, null);
        return ApiResponse.ok("Bookings retrieved successfully", bookingService.searchBookings(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        return ApiResponse.ok("Booking retrieved successfully", bookingService.getBookingById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @RequestBody @Valid BookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.created("Booking created successfully",
                bookingService.createBooking(request, jwt.getSubject()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Booking cancelled successfully",
                bookingService.cancelBooking(id, jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ApiResponse.ok("Booking deleted successfully", null);
    }
}