package com.ms.test_api.service;


import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Booking;

public interface BookingService {

    Page<BookingDTO> getAllBookings(int page, int size, int userId, String branchName, String username, Boolean status);

    Booking addBooking(Booking booking);

    ResponseEntity<ApiResponse<BookingDTO>> getBookingById(Long id);

    ResponseEntity<ApiResponse<Booking>> updateBooking(Long id, Booking booking);

    ResponseEntity<?> deleteBooking(Long id);

}
