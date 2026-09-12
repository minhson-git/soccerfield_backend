package com.ms.test_api.service;

import java.time.Instant;

public interface TokenBlacklistService {

    /** Thu hồi token. expiresAt để dọn dẹp — quá hạn rồi thì không cần nhớ nữa. */
    void blacklist(String tokenId, Instant expiresAt);

    boolean isBlacklisted(String tokenId);
}