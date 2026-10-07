package com.ms.test_api.util;

import java.util.List;

import com.ms.test_api.exception.BadRequestException;
import com.ms.test_api.exception.ConflictException;

public class PricingRuleValidator {
    public static void validateNew(PriceBand candidate, List<PriceBand> sameDayScopeBands) {
        validateGranularity(candidate.window());
        validateNoOverlap(candidate, sameDayScopeBands);
    }

    private static void validateGranularity(TimeRange window) {
        int slotSeconds = BookingPolicy.SLOT_MINUTES * 60;
        if (window.start().toSecondOfDay() % slotSeconds != 0 || window.end().toSecondOfDay() % slotSeconds != 0) {
            throw new BadRequestException("Start and end times must be on a " + BookingPolicy.SLOT_MINUTES + "-minute mark such as 17:00 or 17:30");
        }
    }

    private static void validateNoOverlap(PriceBand candidate, List<PriceBand> existing) {
        if (existing.stream().anyMatch(band -> candidate.window().overlaps(band.window()))) {
            throw new ConflictException("This price rule overlaps an existing rule for the same day scope");
        }
    }
}
