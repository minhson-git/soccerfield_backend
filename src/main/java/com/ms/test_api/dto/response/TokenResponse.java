package com.ms.test_api.dto.response;

import com.ms.test_api.entity.enums.RoleName;

public record TokenResponse(
        boolean authenticated,
        String token,
        RoleName role,
        Long userId
) {}