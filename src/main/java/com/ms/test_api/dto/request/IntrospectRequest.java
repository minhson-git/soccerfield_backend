package com.ms.test_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record IntrospectRequest(

        @NotBlank(message = "Token is required")
        String token
) {}