package com.ms.test_api.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ms.test_api.entity.Booking;
import com.ms.test_api.entity.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @Override
    @EntityGraph(attributePaths = { "user", "user.role", "field", "field.branch" })
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "user", "user.role", "field", "field.branch" })
    Optional<Booking> findById(Long id);

    /**
     * Các booking đang chiếm chỗ của một sân trong một ngày. Dùng cho availability (Day 9).
     */
    @EntityGraph(attributePaths = { "user", "field" })
    List<Booking> findByField_IdAndBookingDateAndStatusIn(
            Long fieldId, LocalDate bookingDate, Collection<BookingStatus> statuses);

    /**
     * Có booking nào của sân này, ngày này, trùng khung giờ [start, end) không? (Day 10)
     */
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.field.id = :fieldId
            AND b.bookingDate = :bookingDate
            AND b.status IN :statuses
            AND b.startTime < :endTime
            AND b.endTime > :startTime
            """)
    boolean existsOverlapping(
            @Param("fieldId") Long fieldId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<BookingStatus> statuses);
}