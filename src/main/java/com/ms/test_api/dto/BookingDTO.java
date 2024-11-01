package com.ms.test_api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDTO {

    private Long bookingId;
    private UserDTO cccd;
    private FieldDTO fieldID;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDate bookingDate;
    private String status;
    
    public BookingDTO(Long bookingId, UserDTO cccd, FieldDTO fieldID, LocalDateTime startTime, LocalDateTime endTime,
            LocalDate bookingDate, String status) {
        this.bookingId = bookingId;
        this.cccd = cccd;
        this.fieldID = fieldID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingDate = bookingDate;
        this.status = status;
    }

}
