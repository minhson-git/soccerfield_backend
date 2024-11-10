package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.BookingDTO;
import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.FieldDTO;
import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.specification.BookingSpecification;
import com.ms.test_api.exception.BookingNotFoundException;
import com.ms.test_api.exception.FieldNotFoundException;
import com.ms.test_api.exception.UserNotFoundException;
import com.ms.test_api.modal.Booking;
import com.ms.test_api.modal.Field;
import com.ms.test_api.modal.UserSoccerField;
import com.ms.test_api.reponsitory.BookingRepository;
import com.ms.test_api.reponsitory.FieldRepository;
import com.ms.test_api.reponsitory.UserReponsitory;
import com.ms.test_api.service.BookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;

    private final FieldRepository fieldRepository;

    private final UserReponsitory userReponsitory;

    @Override
    public Page<BookingDTO> getAllBookings(int page, int size, int userId, String branchName, String username, Boolean status) {
        
        Pageable pageable = PageRequest.of(page, size);

        Specification<Booking> spec = BookingSpecification.filterBookings(userId, branchName, username, status);

        Page<Booking> pageBooking = bookingRepository.findAll(spec, pageable);
    
        return pageBooking.map(b -> new BookingDTO(
                b.getBookingId(),
                new UserDTO(
                    b.getUser().getUserId(),
                    b.getUser().getCitizenId(),
                    b.getUser().getUsername(),
                    b.getUser().getEmail(),
                    b.getUser().getFullname(), 
                    b.getUser().getPhone(),
                    new RoleDTO(
                        b.getUser().getRole().getId(), 
                        b.getUser().getRole().getName())),
                new FieldDTO(
                    b.getField().getFieldId(), 
                    b.getField().getFieldType(),
                    b.getField().getPricePerHour(),
                    b.getField().isStatus(),
                    new BranchDTO(
                        b.getField().getBranch().getBranchId(), 
                        b.getField().getBranch().getBranchName(), 
                        b.getField().getBranch().getAddress(), 
                        b.getField().getBranch().getPhone())),
                b.getStartTime(),
                b.getEndTime(),
                b.getBookingDate(),
                b.isStatus()
            ));
    }

    @Override
    public Booking addBooking(Booking booking) {

        UserSoccerField user = userReponsitory.findById(booking.getUser().getUserId()).orElseThrow(()-> new UserNotFoundException("User not found"));

        Field hasBooking = fieldRepository.findByFieldId(booking.getField().getFieldId()).orElseThrow(()-> new FieldNotFoundException("Field not exist"));

        if (hasBooking.isStatus() == true){
            throw new RuntimeException("Field has been booked");
        }

        if(hasBooking != null){
            hasBooking.setStatus(true);
        }

        fieldRepository.save(hasBooking);

        booking.setUser(user);

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
                        booking.getUser().getCitizenId(),
                        booking.getUser().getFullname(),
                        booking.getUser().getUsername(),
                        booking.getUser().getEmail(),
                        booking.getUser().getPhone(),
                        new RoleDTO(
                            booking.getUser().getRole().getId(), 
                            booking.getUser().getRole().getName())),
                    new FieldDTO(
                        booking.getField().getFieldId(), 
                        booking.getField().getFieldType(),
                        booking.getField().getPricePerHour(),
                        booking.getField().isStatus(),
                        new BranchDTO(
                            booking.getField().getBranch().getBranchId(), 
                            booking.getField().getBranch().getBranchName(), 
                            booking.getField().getBranch().getAddress(), 
                            booking.getField().getBranch().getPhone())),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingDate(),
                booking.isStatus());
            
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
            
            Field fieldType = fieldRepository.findByFieldId(bookingDetails.getField().getFieldId()).orElseThrow(() -> new FieldNotFoundException("Field not found"));
            
            booking.setBookingId(booking.getBookingId());
            booking.setUser(booking.getUser());
            booking.setField(new Field(fieldType.getFieldId(), fieldType.getFieldType(), fieldType.getPricePerHour(), fieldType.isStatus(), fieldType.getBranch()));
            booking.setStartTime(booking.getStartTime());
            booking.setEndTime(booking.getEndTime());
            booking.setBookingDate(booking.getBookingDate());
            booking.setStatus(bookingDetails.isStatus());
            
            bookingRepository.save(booking);
            
            Field field = fieldRepository.findByFieldId(booking.getField().getFieldId()).orElseThrow(() -> new FieldNotFoundException("Field not found"));


            if(booking.isStatus() == false) {
                field.setStatus(true);
            } else {
                field.setStatus(false);
            }

            fieldRepository.save(field);

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
