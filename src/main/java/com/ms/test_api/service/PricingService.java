package com.ms.test_api.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import com.ms.test_api.entity.Field;
import com.ms.test_api.util.PriceBand;
import com.ms.test_api.util.TimeRange;

public interface PricingService {

    /**
     * Giá cuối cùng của một booking: nạp các PriceBand của chi nhánh rồi giao
     * cho {@link com.ms.test_api.util.PriceCalculator} tính.
     *
     * <p>Hàm này thay thế {@code calculateTemporaryPrice} trong
     * {@code BookingServiceImpl}.
     *
     * <p>Lưu ý: gọi hàm này TRONG cùng transaction đã khoá dòng Field, và giá
     * trả về được chốt cứng vào {@code booking.totalPrice} (quyết định D7 ở
     * Day 8). Chủ sân đổi bảng giá ngày mai thì booking cũ không đổi giá.
     */
    BigDecimal calculateTotalPrice(Field field, LocalDate bookingDate, TimeRange requested);
    
    List<PriceBand> bandsFor(Long branchId, DayOfWeek dayOfWeek);
}
