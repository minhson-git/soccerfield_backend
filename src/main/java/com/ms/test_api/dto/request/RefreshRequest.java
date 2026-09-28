package com.ms.test_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}