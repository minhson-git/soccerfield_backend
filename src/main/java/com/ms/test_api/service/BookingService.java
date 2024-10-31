package com.ms.test_api.service;

import java.util.*;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.model.Booking;

public interface BookingService {

    List<BookingDTO> getAllBookings();

    Booking addBooking(Booking booking);

    ResponseEntity<BookingDTO> getBookingById(Long id);

    ResponseEntity<Booking> updateBooking(Long id, Booking booking);

    ResponseEntity<?> deleteBooking(Long id);

}
