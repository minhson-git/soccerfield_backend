package com.ms.test_api.mapper;

import org.springframework.stereotype.Component;

import com.ms.test_api.dto.response.BookingResponse;
import com.ms.test_api.entity.Booking;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final UserMapper userMapper;
    private final FieldMapper fieldMapper;

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingResponse(
                booking.getId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getTotalPrice(),
                userMapper.toSummary(booking.getUser()),
                fieldMapper.toResponse(booking.getField()));
    }
}