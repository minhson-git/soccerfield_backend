package com.ms.test_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(

        @NotBlank(message = "Identifier is required")
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {}