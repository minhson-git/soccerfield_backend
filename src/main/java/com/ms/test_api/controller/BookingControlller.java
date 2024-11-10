package com.ms.test_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Booking;
import com.ms.test_api.service.impl.BookingServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingControlller {

    private final BookingServiceImpl bookingServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingDTO>>> getAllBookings(@RequestParam(required = false, defaultValue = "0") int page,
                            @RequestParam(required = false, defaultValue = "10") int size,
                            @RequestParam(required = false, defaultValue = "0") Integer userId,
                            @RequestParam(required = false, defaultValue = "") String branchName,
                            @RequestParam(required = false, defaultValue = "") String username,
                            @RequestParam(required = false, defaultValue = "") Boolean status){
        try {
            Page<BookingDTO> bookingDTOs = bookingServiceImpl.getAllBookings(page, size, userId, branchName, username, status);
            ApiResponse<Page<BookingDTO>> response = new ApiResponse<>(
                "Successfully retrieved booking data",
                HttpStatus.OK.value(),
                bookingDTOs
            );
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            // TODO: handle exception
            ApiResponse<Page<BookingDTO>> response = new ApiResponse<>(
                "Failed to retrieve booking data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> addBooking(@RequestBody Booking booking){
        try {
            bookingServiceImpl.addBooking(booking);
            ApiResponse<Booking> response = new ApiResponse<Booking>(
                "Booking created successfully", 
                HttpStatus.CREATED.value(), 
                null
            ); 
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Booking> response = new ApiResponse<>(
                "Failed to create booking",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable Long id){
        return bookingServiceImpl.getBookingById(id);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> updateBooking(@PathVariable Long id, @RequestBody Booking booking){
        return bookingServiceImpl.updateBooking(id, booking);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id){
        return bookingServiceImpl.deleteBooking(id);
    }


}
