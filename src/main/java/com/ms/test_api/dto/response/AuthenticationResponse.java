package com.ms.test_api.dto.response;

import com.ms.test_api.entity.enums.RoleName;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        RoleName role,
        Long userId
) {}