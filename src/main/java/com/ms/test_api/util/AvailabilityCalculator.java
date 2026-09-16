package com.ms.test_api.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sinh lưới khung giờ cho một ngày và đánh dấu ô nào còn trống.
 * Pure function — không chạm database, không phụ thuộc Spring.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    /**
     * @param openingHours giờ mở cửa của chi nhánh
     * @param slotMinutes  độ dài mỗi ô, tính bằng phút
     * @param occupied     các khoảng đã bị booking chiếm
     * @param notBefore    ô bắt đầu trước mốc này coi như không đặt được;
     *                     null nếu không giới hạn (ngày trong tương lai)
     */
    public static List<AvailabilitySlot> calculate(
            TimeRange openingHours,
            int slotMinutes,
            List<TimeRange> occupied,
            LocalTime notBefore) {

        List<AvailabilitySlot> slots = new ArrayList<>();
        LocalTime cursor = openingHours.start();

        while (true) {
            LocalTime slotEnd = cursor.plusMinutes(slotMinutes);

            if (slotEnd.isAfter(openingHours.end()) || !slotEnd.isAfter(cursor)) {
                break;
            }
            TimeRange slotRange = new TimeRange(cursor, slotEnd);

            boolean busy = occupied.stream().anyMatch(occupiedRange -> occupiedRange.overlaps(slotRange));
            boolean tooLate = notBefore != null && !cursor.isAfter(notBefore);

            slots.add(new AvailabilitySlot(cursor, slotEnd, !busy && !tooLate));
            cursor = slotEnd;
        }

        return slots;
    }
}