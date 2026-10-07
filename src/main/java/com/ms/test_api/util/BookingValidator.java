package com.ms.test_api.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ms.test_api.entity.Branch;
import com.ms.test_api.exception.BadRequestException;

/**
 * Kiểm tra một yêu cầu đặt sân có hợp lệ về mặt thời gian hay không.
 * Không kiểm tra trùng lịch — việc đó cần database nên nằm ở service.
 */
public final class BookingValidator {

    private BookingValidator() {
    }

    public static void validate(LocalDate bookingDate, TimeRange requested, Branch branch, LocalDateTime now) {
        validateDate(bookingDate, now.toLocalDate());
        validateGranularity(requested);
        validateDuration(requested);
        validateWithinOpeningHours(requested, branch);
        validateNotInPast(bookingDate, requested, now);
    }

    private static void validateDate(LocalDate bookingDate, LocalDate today) {
        if (bookingDate.isBefore(today)) {
            throw new BadRequestException("Booking date cannot be in the past");
        }
        if (bookingDate.isAfter(today.plusDays(BookingPolicy.MAX_ADVANCE_DAYS))) {
            throw new BadRequestException("Bookings can be made at most %d days in advance (latest date: %s)"
                    .formatted(BookingPolicy.MAX_ADVANCE_DAYS, today.plusDays(BookingPolicy.MAX_ADVANCE_DAYS)));
        }
    }

    private static void validateGranularity(TimeRange requested) {
        if (requested.start().toSecondOfDay() % (BookingPolicy.SLOT_MINUTES * 60) != 0) {
            throw new BadRequestException("Start time %s is invalid, it must be on a %d-minute mark such as 18:00 or 18:30"
                    .formatted(requested.start(), BookingPolicy.SLOT_MINUTES));
        }
        if (requested.end().toSecondOfDay() % (BookingPolicy.SLOT_MINUTES * 60) != 0) {
            throw new BadRequestException("End time %s is invalid, it must be on a %d-minute mark such as 19:00 or 19:30"
                    .formatted(requested.end(), BookingPolicy.SLOT_MINUTES));
        }
    }

    private static void validateDuration(TimeRange requested) {
        long duration = requested.durationMinutes();
        if (duration < BookingPolicy.MIN_DURATION_MINUTES || duration > BookingPolicy.MAX_DURATION_MINUTES) {
            throw new BadRequestException("Booking duration must be between %d and %d minutes, requested %d minutes"
                    .formatted(BookingPolicy.MIN_DURATION_MINUTES, BookingPolicy.MAX_DURATION_MINUTES, duration));
        }
    }

    private static void validateWithinOpeningHours(TimeRange requested, Branch branch) {
        TimeRange openingHours = new TimeRange(branch.getOpeningTime(), branch.getClosingTime());
        if (!openingHours.contains(requested)) {
            throw new BadRequestException("Booking must be within the branch opening hours %s - %s"
                    .formatted(openingHours.start(), openingHours.end()));
        }
    }

    private static void validateNotInPast(LocalDate bookingDate, TimeRange requested, LocalDateTime now) {
        if (bookingDate.isEqual(now.toLocalDate())) {
            LocalTime nowTime = now.toLocalTime();
            if (!requested.start().isAfter(nowTime)) {
                throw new BadRequestException("Start time %s has already passed today"
                        .formatted(requested.start()));
            }
        }
    }
}