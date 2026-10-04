package com.ms.test_api.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.dto.request.FieldRequest;
import com.ms.test_api.dto.response.AvailabilitySlotResponse;
import com.ms.test_api.dto.response.FieldAvailabilityResponse;
import com.ms.test_api.dto.response.FieldResponse;
import com.ms.test_api.dto.response.PriceQuoteResponse;
import com.ms.test_api.entity.Branch;
import com.ms.test_api.entity.Field;
import com.ms.test_api.entity.enums.BookingStatus;
import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.exception.BadRequestException;
import com.ms.test_api.exception.ResourceNotFoundException;
import com.ms.test_api.mapper.FieldMapper;
import com.ms.test_api.repository.BookingRepository;
import com.ms.test_api.repository.BranchRepository;
import com.ms.test_api.repository.FieldRepository;
import com.ms.test_api.repository.specification.FieldFilter;
import com.ms.test_api.repository.specification.FieldSpecification;
import com.ms.test_api.service.FieldService;
import com.ms.test_api.service.PricingService;
import com.ms.test_api.util.AvailabilityCalculator;
import com.ms.test_api.util.AvailabilitySlot;
import com.ms.test_api.util.BookingPolicy;
import com.ms.test_api.util.BookingValidator;
import com.ms.test_api.util.PriceBand;
import com.ms.test_api.util.PriceCalculator;
import com.ms.test_api.util.TimeRange;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService {

    private final FieldRepository fieldRepository;
    private final BranchRepository branchRepository;
    private final BookingRepository bookingRepository;
    private final FieldMapper fieldMapper;

    private final PricingService pricingService;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Page<FieldResponse> searchFields(FieldFilter filter, Pageable pageable) {
        return fieldRepository.findAll(FieldSpecification.filter(filter), pageable)
                .map(fieldMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FieldResponse getFieldById(Long id) {
        return fieldMapper.toResponse(findField(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FieldAvailabilityResponse getAvailability(Long fieldId, LocalDate date) {

        Field field = findField(fieldId);
        if (date.isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("Date cannot be in the past");
        }
        if (date.isAfter(LocalDate.now(clock).plusDays(BookingPolicy.MAX_ADVANCE_DAYS))) {
            throw new BadRequestException(
                    "Date cannot be more than " + BookingPolicy.MAX_ADVANCE_DAYS + " days in the future");
        }

        Branch branch = field.getBranch();
        TimeRange openingHours = new TimeRange(branch.getOpeningTime(), branch.getClosingTime());
        LocalDate today = LocalDate.now(clock);

        // Sân không ACTIVE: coi như cả ngày đã bị chiếm, không ô nào đặt được.
        List<TimeRange> occupied = field.getStatus() == FieldStatus.ACTIVE
                ? loadOccupiedRanges(fieldId, date)
                : List.of(openingHours);

        // Chỉ chặn các ô đã qua khi xem lịch của chính hôm nay.
        LocalTime notBefore = date.isEqual(today) ? LocalTime.now(clock) : null;

        List<AvailabilitySlot> slots = AvailabilityCalculator.calculate(
                openingHours, BookingPolicy.SLOT_MINUTES, occupied, notBefore);

        List<PriceBand> bands = pricingService.bandsFor(branch.getId(), date.getDayOfWeek());

        return new FieldAvailabilityResponse(
                field.getId(),
                field.getName(),
                date,
                openingHours.start(),
                openingHours.end(),
                BookingPolicy.SLOT_MINUTES,
                BookingPolicy.MIN_DURATION_MINUTES,
                BookingPolicy.MAX_DURATION_MINUTES,
                slots.stream()
                        .map(slot -> new AvailabilitySlotResponse(
                                slot.startTime(), slot.endTime(), slot.available(),
                                PriceCalculator.hourlyRateAt(field.getBasePrice(), slot.startTime(),
                                        date.getDayOfWeek(), bands)))
                        .toList());
    }

    private List<TimeRange> loadOccupiedRanges(Long fieldId, LocalDate date) {
        return bookingRepository
                .findByField_IdAndBookingDateAndStatusIn(fieldId, date, BookingStatus.OCCUPYING)
                .stream()
                .map(booking -> new TimeRange(booking.getStartTime(), booking.getEndTime()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PriceQuoteResponse quote(Long fieldId, LocalDate date, LocalTime start, LocalTime end) {
        Field field = findField(fieldId);
        TimeRange requested = toTimeRange(start, end);
        BookingValidator.validate(date, requested, field.getBranch(), LocalDateTime.now(clock));
        BigDecimal total = pricingService.calculateTotalPrice(field, date, requested);
        return new PriceQuoteResponse(fieldId, date, start, end, requested.durationMinutes(), total);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @branchSecurity.ownsBranch(#request.branchId(), authentication.name)")
    public FieldResponse createField(FieldRequest request) {
        Field field = new Field();
        fieldMapper.applyRequest(field, request);
        field.setBranch(findBranch(request.branchId()));
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @branchSecurity.ownsField(#id, authentication.name)")
    public FieldResponse updateField(Long id, FieldRequest request) {
        Field field = findField(id);

        if (!request.branchId().equals(field.getBranch().getId())) {
            throw new BadRequestException("Moving a field to another branch is not supported");
        }

        fieldMapper.applyRequest(field, request);
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteField(Long id) {
        fieldRepository.delete(findField(id));
    }

    private Field findField(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + id));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }

    private TimeRange toTimeRange(LocalTime start, LocalTime end) {
        try {
            return new TimeRange(start, end);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}