package com.ms.test_api.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.entity.Booking;
import com.ms.test_api.entity.Branch;
import com.ms.test_api.entity.Field;
import com.ms.test_api.entity.PricingRule;
import com.ms.test_api.entity.Role;
import com.ms.test_api.entity.User;
import com.ms.test_api.entity.enums.BookingStatus;
import com.ms.test_api.entity.enums.FieldStatus;
import com.ms.test_api.entity.enums.FieldType;
import com.ms.test_api.entity.enums.RoleName;
import com.ms.test_api.repository.BookingRepository;
import com.ms.test_api.repository.BranchRepository;
import com.ms.test_api.repository.FieldRepository;
import com.ms.test_api.repository.PricingRuleRepository;
import com.ms.test_api.repository.RoleRepository;
import com.ms.test_api.repository.UserRepository;
import com.ms.test_api.service.PricingService;
import com.ms.test_api.util.PhoneNumbers;
import com.ms.test_api.util.TimeRange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Nạp dữ liệu demo cho môi trường dev / test thủ công.
 *
 * <p>
 * <b>Chỉ chạy khi {@code app.seed.demo=true}</b> (biến môi trường
 * {@code APP_SEED_DEMO=true}). Mặc định tắt: production không bao giờ được có
 * tài khoản mật khẩu {@code password123}.
 *
 * <p>
 * <b>Vì sao viết bằng Java chứ không bằng file SQL:</b>
 * <ul>
 * <li>Mật khẩu được mã hoá bằng chính {@code PasswordEncoder} của ứng dụng,
 * không phải dán một chuỗi BCrypt chép từ đâu đó — loại hash "mẫu" đó
 * thường không khớp với mật khẩu ghi kèm.
 * <li>Số điện thoại đi qua {@link PhoneNumbers#normalizeVietnamese}, giống
 * hệt luồng đăng ký. Chèn thẳng {@code 0901234567} bằng SQL sẽ khiến
 * đăng nhập bằng số điện thoại không bao giờ tìm thấy user, vì backend
 * tìm theo dạng {@code +84901234567}.
 * <li>Giá booking tính bằng {@link PricingService} thật, nên khớp đúng với
 * {@code /quote} và với giá khi đặt qua API.
 * <li>Ngày của booking tính theo {@link Clock}, nên seed lúc nào cũng có
 * booking "ngày mai" thật sự là ngày mai.
 * </ul>
 *
 * <p>
 * <b>Chạy một lần:</b> nếu database đã có chi nhánh thì bỏ qua toàn bộ.
 * Muốn nạp lại thì {@code docker compose down -v} rồi khởi động lại.
 *
 * <p>
 * Chạy SAU {@link DataInitializer} ({@code @Order(2)}), vì cần các role
 * mà DataInitializer tạo ra.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed.demo", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    /** Mật khẩu chung của mọi tài khoản demo. */
    static final String DEMO_PASSWORD = "password123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final FieldRepository fieldRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final BookingRepository bookingRepository;
    private final PricingService pricingService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (branchRepository.count() > 0) {
            log.info("Demo seed skipped: database already contains branches");
            return;
        }

        log.warn("Seeding DEMO data (app.seed.demo=true). Never enable this in production.");

        // ---------- Người dùng ----------
        User customer1 = user("customer1", "Trần Văn A", "0901234567", RoleName.CUSTOMER);
        User customer2 = user("customer2", "Lê Thị B", "0912345678", RoleName.CUSTOMER);
        User owner1 = user("owner1", "Nguyễn Chủ Sân", "0907654321", RoleName.OWNER);
        User owner2 = user("owner2", "Phạm Chủ Sân", "0938765432", RoleName.OWNER);

        // ---------- Chi nhánh ----------
        // owner1 và owner2 mỗi người một chi nhánh, để test được trường hợp
        // "OWNER nhưng không phải chủ sân này" (phải nhận 403).
        Branch quan1 = branch("Chi nhánh Quận 1", "12 Nguyễn Huệ", "Quận 1",
                "02838221234", "06:00", "22:00", owner1);
        Branch thuDuc = branch("Chi nhánh Thủ Đức", "45 Võ Văn Ngân", "Thủ Đức",
                "02838225678", "06:00", "23:00", owner2);

        // ---------- Sân ----------
        // Thứ tự lưu quyết định id: database mới thì sân 1..5 đúng như dưới.
        // docs/TESTING.md và bộ Postman dựa vào các id này.
        Field san5A = field("Sân 5A", FieldType.FIVE_A_SIDE, "300000", FieldStatus.ACTIVE, quan1); // id 1
        Field san5B = field("Sân 5B", FieldType.FIVE_A_SIDE, "300000", FieldStatus.ACTIVE, quan1); // id 2
        field("Sân 7A", FieldType.SEVEN_A_SIDE, "500000", FieldStatus.ACTIVE, quan1); // id 3
        field("Sân 11", FieldType.ELEVEN_A_SIDE, "900000", FieldStatus.MAINTENANCE, quan1); // id 4
        field("Sân 5C", FieldType.FIVE_A_SIDE, "280000", FieldStatus.ACTIVE, thuDuc); // id 5

        // ---------- Bảng giá ----------
        // Quận 1: giờ thường, cao điểm, và cuối tuần đắt hơn (band theo ngày ghi đè
        // band chung).
        rule(quan1, null, "06:00", "17:00", "1.00");
        rule(quan1, null, "17:00", "22:00", "1.50");
        rule(quan1, DayOfWeek.SATURDAY, "17:00", "22:00", "2.00");
        rule(quan1, DayOfWeek.SUNDAY, "17:00", "22:00", "2.00");
        // Thủ Đức: giá phẳng cả ngày.
        rule(thuDuc, null, "06:00", "23:00", "1.00");

        // ---------- Booking mẫu ----------
        // Tất cả nằm ở SÂN 2 (5B), để không đụng các request mẫu trong Postman
        // (dùng sân 1) và lệnh test đặt đồng thời trong TESTING.md (sân 2,
        // 20:00–21:00).
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plusDays(1);

        booking(customer2, san5B, tomorrow, "08:00", "10:00", BookingStatus.CONFIRMED);
        // PENDING này sẽ tự chuyển EXPIRED khoảng 30 phút sau khi khởi động
        // (createdAt = lúc seed) — dùng để quan sát quy tắc F9.
        booking(customer2, san5B, tomorrow, "10:00", "11:00", BookingStatus.PENDING);
        booking(customer2, san5B, today.plusDays(2), "18:00", "19:00", BookingStatus.CANCELLED);
        booking(customer1, san5B, today.minusDays(1), "18:00", "19:00", BookingStatus.COMPLETED);

        log.warn("Demo data ready: 4 users (password '{}'), 2 branches, 5 fields, 5 pricing rules, 4 bookings",
                DEMO_PASSWORD);
    }

    // ------------------------------------------------------------------

    private User user(String username, String fullName, String rawPhone, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " is not seeded"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@demo.local");
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        user.setFullName(fullName);
        user.setPhone(PhoneNumbers.normalizeVietnamese(rawPhone));
        user.setEnabled(true);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Branch branch(String name, String address, String district, String phone,
            String opening, String closing, User owner) {
        Branch branch = new Branch();
        branch.setName(name);
        branch.setAddress(address);
        branch.setDistrict(district);
        branch.setPhone(phone);
        branch.setOpeningTime(LocalTime.parse(opening));
        branch.setClosingTime(LocalTime.parse(closing));
        branch.setOwner(owner);
        return branchRepository.save(branch);
    }

    private Field field(String name, FieldType type, String basePrice, FieldStatus status, Branch branch) {
        Field field = new Field();
        field.setName(name);
        field.setFieldType(type);
        field.setBasePrice(new BigDecimal(basePrice));
        field.setStatus(status);
        field.setBranch(branch);
        return fieldRepository.save(field);
    }

    private void rule(Branch branch, DayOfWeek day, String start, String end, String multiplier) {
        PricingRule rule = new PricingRule();
        rule.setBranch(branch);
        rule.setDayOfWeek(day);
        rule.setStartTime(LocalTime.parse(start));
        rule.setEndTime(LocalTime.parse(end));
        rule.setMultiplier(new BigDecimal(multiplier));
        pricingRuleRepository.save(rule);
    }

    private void booking(User user, Field field, LocalDate date, String start, String end, BookingStatus status) {
        TimeRange range = new TimeRange(LocalTime.parse(start), LocalTime.parse(end));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setField(field);
        booking.setBookingDate(date);
        booking.setStartTime(range.start());
        booking.setEndTime(range.end());
        booking.setStatus(status);
        booking.setTotalPrice(pricingService.calculateTotalPrice(field, date, range));
        bookingRepository.save(booking);
    }
}
