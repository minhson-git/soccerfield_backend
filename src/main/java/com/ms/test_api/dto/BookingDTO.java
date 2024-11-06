package com.ms.test_api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDTO {

    private Long bookingId;
    private UserDTO user;
    private FieldDTO field;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDate bookingDate;
    private boolean status;
    
    public BookingDTO(Long bookingId, UserDTO cccd, FieldDTO fieldID, LocalDateTime startTime, LocalDateTime endTime,
            LocalDate bookingDate, boolean status) {
        this.bookingId = bookingId;
        this.user = cccd;
        this.field = fieldID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingDate = bookingDate;
        this.status = status;
    }

}
