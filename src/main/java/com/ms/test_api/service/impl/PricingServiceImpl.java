package com.ms.test_api.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.entity.Field;
import com.ms.test_api.repository.PricingRuleRepository;
import com.ms.test_api.service.PricingService;
import com.ms.test_api.util.BookingPolicy;
import com.ms.test_api.util.PriceBand;
import com.ms.test_api.util.PriceCalculator;
import com.ms.test_api.util.TimeRange;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPrice(Field field, LocalDate bookingDate, TimeRange requested) {
        return PriceCalculator.calculate(
                field.getBasePrice(), BookingPolicy.SLOT_MINUTES, requested,
                bookingDate.getDayOfWeek(), bandsFor(field.getBranch().getId(), bookingDate.getDayOfWeek()));
    }

    @Override
    public List<PriceBand> bandsFor(Long branchId, DayOfWeek dayOfWeek) {
                List<PriceBand> bands = pricingRuleRepository
                .findApplicable(branchId, dayOfWeek)
                .stream()
                .map(rule -> new PriceBand(rule.getDayOfWeek(),
                        new TimeRange(rule.getStartTime(), rule.getEndTime()),
                        rule.getMultiplier()))
                .toList();

        return bands;
    }
}
