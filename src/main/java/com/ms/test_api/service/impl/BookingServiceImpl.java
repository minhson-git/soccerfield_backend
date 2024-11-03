package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.exception.BookingNotFoundException;
import com.ms.test_api.model.Booking;
import com.ms.test_api.reponsitory.BookingRepository;
import com.ms.test_api.service.BookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;

    @Override
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
            .map(b -> new BookingDTO(
                b.getBookingId(),
                new UserDTO(b.getUser().getCCCD(), b.getUser().getFullname(), 
                new RoleDTO(b.getUser().getRole().getId(), b.getUser().getRole().getName())),
                new FieldDTO(b.getField().getFieldId(), b.getField().getFieldType()),
                b.getStartTime(),
                b.getEndTime(),
                b.getBookingDate(),
                b.getStatus()
            )).collect(Collectors.toList());
    }

    @Override
    public Booking addBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public ResponseEntity<BookingDTO> getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException("Booking not exist with id: "+id));
        BookingDTO bookingDTO = new BookingDTO(
            booking.getBookingId(),
            new UserDTO(booking.getUser().getCCCD(), booking.getUser().getFullname(), 
            new RoleDTO(booking.getUser().getRole().getId(), booking.getUser().getRole().getName())),
            new FieldDTO(booking.getField().getFieldId(), booking.getField().getFieldType()),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getBookingDate(),
            booking.getStatus()
        );
        return ResponseEntity.ok(bookingDTO);
    }

    @Override
    public ResponseEntity<Booking> updateBooking(Long id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()-> new BookingNotFoundException("Booking not exist with id: "+id));
        booking.setBookingId(bookingDetails.getBookingId());
        booking.setUser(bookingDetails.getUser());
        booking.setField(bookingDetails.getField());
        booking.setStartTime(bookingDetails.getStartTime());
        booking.setEndTime(bookingDetails.getEndTime());
        booking.setBookingDate(bookingDetails.getBookingDate());
        booking.setStatus(bookingDetails.getStatus());

        Booking updateBooking = bookingRepository.save(booking);
        return ResponseEntity.ok(updateBooking);
    }

    @Override
    public ResponseEntity<?> deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()-> new BookingNotFoundException("Booking not exist with id: "+id));
        bookingRepository.delete(booking);
        return ResponseEntity.ok(booking);
    }

}
