package com.ms.test_api.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;

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
import com.ms.test_api.service.PricingService;
import com.ms.test_api.util.BookingPolicy;
import com.ms.test_api.util.BookingValidator;
import com.ms.test_api.util.TimeRange;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final PricingService pricingService;

    private final Clock clock;

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

        // Khoá dòng Field — xem §4. Phải đứng TRƯỚC mọi kiểm tra trùng.
        Field field = fieldRepository.findByIdForUpdate(request.fieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + request.fieldId()));

        if (field.getStatus() != FieldStatus.ACTIVE) {
            throw new BadRequestException("Field is not available for booking");
        }

        TimeRange requested = toTimeRange(request);
        BookingValidator.validate(request.bookingDate(), requested, field.getBranch(), LocalDateTime.now(clock));

        if (bookingRepository.existsOverlapping(
                field.getId(),
                request.bookingDate(),
                requested.start(),
                requested.end(),
                BookingStatus.OCCUPYING)) {
            throw new ConflictException("This time slot has already been booked");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setField(field);
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(requested.start());
        booking.setEndTime(requested.end());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(pricingService.calculateTotalPrice(field, request.bookingDate(), requested));

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long id, String ownerUsername) {
        Booking booking = findBookingForUpdate(id);
        requireFieldOwner(booking, ownerUsername);

        switch (booking.getStatus()) {
            case PENDING -> {
            }
            case CONFIRMED -> throw new ConflictException("Booking is already confirmed");
            case EXPIRED -> throw new ConflictException(
                    "Booking has expired and cannot be confirmed; the slot may have been taken");
            default -> throw new ConflictException("Cannot confirm a booking with status " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long id, String ownerUsername) {
        Booking booking = findBookingForUpdate(id);
        requireFieldOwner(booking, ownerUsername);

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Cannot reject a booking with status " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, String username) {
        Booking booking = findBookingForUpdate(id);

        if (!booking.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You can only cancel your own booking");
        }
        switch (booking.getStatus()) {
            case PENDING, CONFIRMED -> {
            }
            case CANCELLED -> throw new ConflictException("Booking is already cancelled");
            default -> throw new ConflictException(
                    "Cannot cancel a booking with status " + booking.getStatus());
        }

        LocalDateTime kickoff = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime now = LocalDateTime.now(clock);

        if (!now.isBefore(kickoff)) {
            throw new ConflictException("Booking has already started and cannot be cancelled");
        }
        if (now.isAfter(kickoff.minusHours(BookingPolicy.CANCEL_DEADLINE_HOURS))) {
            throw new ConflictException("Booking cannot be cancelled within "
                    + BookingPolicy.CANCEL_DEADLINE_HOURS + " hours of kick-off");
        }

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

    private Booking findBookingForUpdate(Long id) {
        return bookingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    private void requireFieldOwner(Booking booking, String username) {
        User owner = booking.getField().getBranch().getOwner();
        if (owner == null || !owner.getUsername().equals(username)) {
            throw new AccessDeniedException("You can only manage bookings for your own fields");
        }
    }

    private TimeRange toTimeRange(BookingRequest request) {
        try {
            return new TimeRange(request.startTime(), request.endTime());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}