package com.ms.test_api.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ms.test_api.entity.PricingRule;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByBranch_IdOrderByDayOfWeekAscStartTimeAsc(Long branchId);

    /**
     * Các rule có thể áp dụng cho một ngày cụ thể: rule của đúng thứ đó,
     * cộng với các rule chung (dayOfWeek IS NULL).
     *
     * <p>Lọc ngay ở database thay vì tải hết rồi lọc trong Java: một chi nhánh
     * có thể có 20+ rule, mà mỗi lần tính giá chỉ cần 2-3 rule.
     *
     * <p>Lưu ý cú pháp: phải viết {@code (:dayOfWeek IS NULL OR ...)} kiểu này
     * chứ không dùng {@code = :dayOfWeek} với giá trị null, vì trong SQL
     * {@code NULL = NULL} cho ra UNKNOWN chứ không phải TRUE.
     */
    @Query("""
            SELECT r FROM PricingRule r
            WHERE r.branch.id = :branchId
            AND (r.dayOfWeek = :dayOfWeek OR r.dayOfWeek IS NULL)
            """)
    List<PricingRule> findApplicable(
            @Param("branchId") Long branchId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Các rule cùng "phạm vi ngày" với rule đang muốn tạo — dùng để kiểm tra
     * chồng giờ (§5). Rule chung chỉ xung đột với rule chung; rule T7 chỉ xung
     * đột với rule T7. Rule T7 chồng giờ rule chung là HỢP LỆ, đó chính là
     * cơ chế ghi đè của D11.
     */
    @Query("""
            SELECT r FROM PricingRule r
            WHERE r.branch.id = :branchId
            AND ((:dayOfWeek IS NULL AND r.dayOfWeek IS NULL)
                 OR r.dayOfWeek = :dayOfWeek)
            """)
    List<PricingRule> findSameDayScope(
            @Param("branchId") Long branchId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
