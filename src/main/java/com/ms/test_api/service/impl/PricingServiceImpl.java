package com.ms.test_api.service.impl;

import java.math.BigDecimal;
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
        List<PriceBand> bands = pricingRuleRepository
                .findApplicable(field.getBranch().getId(), bookingDate.getDayOfWeek())
                .stream()
                .map(rule -> new PriceBand(rule.getDayOfWeek(),
                        new TimeRange(rule.getStartTime(), rule.getEndTime()),
                        rule.getMultiplier()))
                .toList();

        return PriceCalculator.calculate(
                field.getBasePrice(), BookingPolicy.SLOT_MINUTES, requested,
                bookingDate.getDayOfWeek(), bands);
    }
}
