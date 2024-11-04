package com.ms.test_api.service;

import java.util.*;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Booking;

public interface BookingService {

    List<BookingDTO> getAllBookings();

    Booking addBooking(Booking booking);

    ResponseEntity<ApiResponse<BookingDTO>> getBookingById(Long id);

    ResponseEntity<ApiResponse<Booking>> updateBooking(Long id, Booking booking);

    ResponseEntity<?> deleteBooking(Long id);

}
