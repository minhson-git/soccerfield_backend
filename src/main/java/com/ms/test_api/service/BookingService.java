package com.ms.test_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ms.test_api.dto.request.BookingRequest;
import com.ms.test_api.dto.response.BookingResponse;
import com.ms.test_api.repository.specification.BookingFilter;

public interface BookingService {

    Page<BookingResponse> searchBookings(BookingFilter filter, Pageable pageable);

    BookingResponse getBookingById(Long id);

    BookingResponse createBooking(BookingRequest request, String username);

    BookingResponse cancelBooking(Long id, String username);

    void deleteBooking(Long id);
}