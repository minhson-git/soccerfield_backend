package com.ms.test_api.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ms.test_api.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    long deleteByExpiresAtBefore(Instant threshold);
}