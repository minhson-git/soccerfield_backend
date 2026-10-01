package com.ms.test_api.util;

import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * Một khung giá đã được bóc ra khỏi entity {@link com.ms.test_api.entity.PricingRule}.
 *
 * <p>Vì sao cần record này thay vì truyền thẳng {@code PricingRule} vào
 * {@link PriceCalculator}? Cùng lý do {@link AvailabilityCalculator} nhận
 * {@code List<TimeRange>} chứ không nhận {@code List<Booking>}: giữ cho lớp
 * tính toán hoàn toàn thuần — không JPA, không lazy loading, không cần Spring
 * context để test. Việc chuyển {@code PricingRule -> PriceBand} là của
 * {@code PricingServiceImpl}.
 *
 * @param dayOfWeek  {@code null} = áp dụng mọi ngày. Có giá trị = chỉ ngày đó,
 *                   và ghi đè band {@code null} trong phần giờ chồng nhau (D11).
 * @param window     khung giờ nửa mở [start, end)
 * @param multiplier hệ số nhân với basePrice
 */
public record PriceBand(DayOfWeek dayOfWeek, TimeRange window, BigDecimal multiplier) {

    public PriceBand {
        if (window == null) {
            throw new IllegalArgumentException("Price band window cannot be null");
        }
        if (multiplier == null || multiplier.signum() <= 0) {
            throw new IllegalArgumentException("Multiplier must be greater than zero");
        }
    }

    /** true nếu band này áp dụng cho ngày đã cho (band chung áp dụng cho mọi ngày). */
    public boolean appliesOn(DayOfWeek day) {
        return dayOfWeek == null || dayOfWeek == day;
    }

    /** true nếu mốc giờ này rơi vào trong khung, theo quy ước nửa mở [start, end). */
    public boolean covers(java.time.LocalTime time) {
        return !time.isBefore(window.start()) && time.isBefore(window.end());
    }

    /** true nếu đây là band chung (mọi ngày), dùng để xếp thứ tự ưu tiên. */
    public boolean isGeneric() {
        return dayOfWeek == null;
    }
}
