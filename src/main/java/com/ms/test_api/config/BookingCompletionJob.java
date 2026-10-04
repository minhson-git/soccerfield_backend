package com.ms.test_api.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.entity.enums.BookingStatus;
import com.ms.test_api.repository.BookingRepository;
import com.ms.test_api.util.BookingPolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Nhả chỗ của những booking PENDING mà chủ sân không xác nhận trong hạn
 * (quyết định D3: {@link BookingPolicy#PENDING_EXPIRY_MINUTES} phút).
 *
 * <p>Vì sao cần job này: PENDING vẫn CHIẾM khung giờ
 * ({@link BookingStatus#OCCUPYING}). Không có cơ chế hết hạn thì một khách bấm
 * đặt rồi bỏ đi sẽ giữ khung giờ đó vĩnh viễn, và chủ sân không bán được nữa.
 *
 * <p>Quét mỗi phút chứ không mỗi 30 phút: như vậy booking hết hạn trong khoảng
 * 30–31 phút chứ không phải 30–60 phút. Câu UPDATE chỉ chạm các dòng thực sự
 * quá hạn nên gần như luôn không khớp dòng nào — chi phí một lần quét là một
 * index lookup, chạy mỗi phút hoàn toàn thoải mái.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BookingCompletionJob {

    private final BookingRepository bookingRepository;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${app.booking.expiry-scan-interval:PT1M}")
    @Transactional
    public void expireStaleCompleteBookings() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime timeNow = now.toLocalTime();

        int completed = bookingRepository.completeFinished(
                BookingStatus.CONFIRMED, BookingStatus.COMPLETED, today, timeNow, now);

        if (completed > 0) {
            log.info("Completed {} confirmed booking(s) before {}", completed, now);
        }
    }
}
