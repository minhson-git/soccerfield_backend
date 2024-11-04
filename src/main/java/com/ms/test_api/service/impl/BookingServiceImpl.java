package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.exception.BookingNotFoundException;
import com.ms.test_api.modal.Booking;
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
                new UserDTO(
                    b.getUser().getUserId(), 
                    b.getUser().getFullname(), 
                    new RoleDTO(
                        b.getUser().getRole().getId(), 
                        b.getUser().getRole().getName())),
                new FieldDTO(
                    b.getField().getFieldId(), 
                    b.getField().getFieldType(), 
                    new BranchDTO(
                        b.getField().getBranch().getBranchId(), 
                        b.getField().getBranch().getBranchName(), 
                        b.getField().getBranch().getAddress(), 
                        b.getField().getBranch().getPhone())),
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
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(Long id) {
        try {
            Booking booking = bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException("Booking not exist with id: "+id));
            BookingDTO bookingDTO = new BookingDTO(
                booking.getBookingId(),
                    new UserDTO(
                        booking.getUser().getUserId(), 
                        booking.getUser().getFullname(), 
                        new RoleDTO(
                            booking.getUser().getRole().getId(), 
                            booking.getUser().getRole().getName())),
                    new FieldDTO(
                        booking.getField().getFieldId(), 
                        booking.getField().getFieldType(), 
                        new BranchDTO(
                            booking.getField().getBranch().getBranchId(), 
                            booking.getField().getBranch().getBranchName(), 
                            booking.getField().getBranch().getAddress(), 
                            booking.getField().getBranch().getPhone())),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingDate(),
                booking.getStatus());
            
            ApiResponse<BookingDTO> response = new ApiResponse<BookingDTO>(
                "Successfully retrieved booking data", 
                HttpStatus.OK.value(), 
                bookingDTO
            );
            return new ResponseEntity<ApiResponse<BookingDTO>>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<BookingDTO> response = new ApiResponse<BookingDTO>(
                "Failed retrieved booking data", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<ApiResponse<BookingDTO>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    public ResponseEntity<ApiResponse<Booking>> updateBooking(Long id, Booking bookingDetails) {
        try {
            Booking booking = bookingRepository.findById(id)
            .orElseThrow(()-> new BookingNotFoundException("Booking not exist with id: "+id));

            booking.setBookingId(booking.getBookingId());
            booking.setUser(booking.getUser());
            booking.setField(booking.getField());
            booking.setStartTime(bookingDetails.getStartTime());
            booking.setEndTime(bookingDetails.getEndTime());
            booking.setBookingDate(bookingDetails.getBookingDate());
            booking.setStatus(bookingDetails.getStatus());

            bookingRepository.save(booking);

            ApiResponse<Booking> response = new ApiResponse<Booking>(
                "Updated successfully booking", 
                HttpStatus.OK.value(),
                null
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Booking> response = new ApiResponse<Booking>(
                "Failed to update booking", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity<?> deleteBooking(Long id) {
        try {
            Booking booking = bookingRepository.findById(id).orElseThrow(()-> new BookingNotFoundException("Booking not exist with id: "+id));
            bookingRepository.delete(booking);
            ApiResponse<String> response = new ApiResponse<String>(
                "Deleted successfully booking", 
                HttpStatus.OK.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<String>(
                "Failed to delete booking", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
