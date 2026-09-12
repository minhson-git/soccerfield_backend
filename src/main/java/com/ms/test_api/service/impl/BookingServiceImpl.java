package com.ms.test_api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.dto.request.BookingRequest;
import com.ms.test_api.dto.response.BookingResponse;
import com.ms.test_api.entity.Booking;
import com.ms.test_api.entity.Field;
import com.ms.test_api.entity.User;
import com.ms.test_api.entity.enums.BookingStatus;
import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.exception.BadRequestException;
import com.ms.test_api.exception.ConflictException;
import com.ms.test_api.exception.ResourceNotFoundException;
import com.ms.test_api.mapper.BookingMapper;
import com.ms.test_api.repository.BookingRepository;
import com.ms.test_api.repository.FieldRepository;
import com.ms.test_api.repository.UserRepository;
import com.ms.test_api.repository.specification.BookingFilter;
import com.ms.test_api.repository.specification.BookingSpecification;
import com.ms.test_api.service.BookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookings(BookingFilter filter, Pageable pageable) {
        return bookingRepository.findAll(BookingSpecification.filter(filter), pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("hasRole('ADMIN') "
            + "or returnObject.user().username() == authentication.name "
            + "or @branchSecurity.ownsBookedField(returnObject.id(), authentication.name)")
    public BookingResponse getBookingById(Long id) {
        return bookingMapper.toResponse(findBooking(id));
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Field field = fieldRepository.findById(request.fieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + request.fieldId()));

        if (field.getStatus() != FieldStatus.ACTIVE) {
            throw new BadRequestException("Field is not available for booking");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // TODO (Week 2 - Day 9): kiểm tra khung giờ nằm trong opening/closing time của branch
        // TODO (Week 2 - Day 10): overlap detection + @Lock(PESSIMISTIC_WRITE) chống double booking
        // TODO (Week 2 - Day 11): tính giá theo PricingRule thay vì basePrice

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setField(field);
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(calculateTemporaryPrice(field, request));

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, String username) {
        Booking booking = findBooking(id);

        if (!booking.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You can only cancel your own booking");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("Completed booking cannot be cancelled");
        }

        // TODO (Week 2 - Day 12): áp dụng chính sách huỷ (hạn chót trước giờ đá)

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.delete(findBooking(id));
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    /** Tạm tính theo basePrice — sẽ được PricingService thay thế ở Day 11. */
    private BigDecimal calculateTemporaryPrice(Field field, BookingRequest request) {
        long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return field.getBasePrice().multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}