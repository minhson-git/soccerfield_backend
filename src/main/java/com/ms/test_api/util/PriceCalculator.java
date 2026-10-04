package com.ms.test_api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class PriceCalculator {
    public static BigDecimal calculate(BigDecimal basePrice, int slotMinutes, TimeRange requested, DayOfWeek dayOfWeek, List<PriceBand> bands) {

        int slotSeconds = slotMinutes * 60;
        int endSecond = requested.end().toSecondOfDay();

        // Cộng dồn theo đơn vị "đồng × phút", chia cho 60 MỘT lần ở cuối.
        BigDecimal minutePriceTotal = BigDecimal.ZERO;

        for (int second = requested.start().toSecondOfDay(); second + slotSeconds <= endSecond; second += slotSeconds) {
            BigDecimal multiplier = resolveMultiplier(LocalTime.ofSecondOfDay(second), dayOfWeek, bands);
            minutePriceTotal = minutePriceTotal.add(basePrice.multiply(multiplier).multiply(BigDecimal.valueOf(slotMinutes)));
        }

        return minutePriceTotal.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal hourlyRateAt(BigDecimal basePrice, LocalTime at, DayOfWeek dayOfWeek, List<PriceBand> bands) {
        BigDecimal multiplier = resolveMultiplier(at, dayOfWeek, bands);
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal resolveMultiplier(LocalTime at, DayOfWeek dayOfWeek, List<PriceBand> bands) {
        for (PriceBand band : bands) {
            if (!band.isGeneric() && band.appliesOn(dayOfWeek) && band.covers(at))
                return band.multiplier();
        }
        for (PriceBand band : bands) {
            if (band.isGeneric() && band.covers(at))
                return band.multiplier();
        }
        return BigDecimal.ONE;
    }
}
        
