package com.ms.test_api.config;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.repository.InvalidatedTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvalidatedTokenCleanupJob {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        long removed = invalidatedTokenRepository.deleteByExpiresAtBefore(Instant.now());
        if (removed > 0) {
            log.info("Purged {} expired invalidated tokens", removed);
        }
    }
}