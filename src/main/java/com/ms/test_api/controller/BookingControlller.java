package com.ms.test_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Booking;
import com.ms.test_api.service.impl.BookingServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingControlller {

    private final BookingServiceImpl bookingServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings(){
        try {
            List<BookingDTO> bookingDTOs = bookingServiceImpl.getAllBookings();
            ApiResponse<List<BookingDTO>> response = new ApiResponse<>(
                "Successfully retrieved branch data",
                HttpStatus.OK.value(),
                bookingDTOs
            );
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            // TODO: handle exception
            ApiResponse<List<BookingDTO>> response = new ApiResponse<>(
                "Failed to retrieve branch data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> addBooking(@RequestBody Booking booking){
        try {
            Booking bookings = bookingServiceImpl.addBooking(booking);
            ApiResponse<Booking> response = new ApiResponse<Booking>(
                "Booking created successfully", 
                HttpStatus.CREATED.value(), 
                bookings
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
