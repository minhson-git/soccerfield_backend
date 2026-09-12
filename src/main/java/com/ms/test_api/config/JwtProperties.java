package com.ms.test_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        @NotBlank
        @Size(min = 64, message = "HS512 requires a signer key of at least 64 bytes")
        String signerKey,

        @NotBlank
        String issuer,

        @Positive
        long accessTokenTtlMinutes,

        @Positive
        long refreshTokenTtlDays
) {}