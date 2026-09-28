package com.ms.test_api.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.entity.InvalidatedToken;
import com.ms.test_api.repository.InvalidatedTokenRepository;
import com.ms.test_api.service.TokenBlacklistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    @Transactional
    public void blacklist(String tokenId, Instant expiresAt) {
        if (!invalidatedTokenRepository.existsById(tokenId)) {
            invalidatedTokenRepository.save(new InvalidatedToken(tokenId, expiresAt));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String tokenId) {
        return invalidatedTokenRepository.existsById(tokenId);
    }
}