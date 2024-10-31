package com.ms.test_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.model.Booking;
import com.ms.test_api.service.impl.BookingServiceImpl;

import lombok.RequiredArgsConstructor;

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
    public List<BookingDTO> getAllBookings(){
        return bookingServiceImpl.getAllBookings();
    }

    @PostMapping
    public Booking addBooking(@RequestBody Booking booking){
        return bookingServiceImpl.addBooking(booking);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id){
        return bookingServiceImpl.getBookingById(id);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking booking){
        return bookingServiceImpl.updateBooking(id, booking);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id){
        return bookingServiceImpl.deleteBooking(id);
    }


}
