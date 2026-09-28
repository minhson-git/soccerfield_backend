package com.ms.test_api.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.ms.test_api.service.JwtService;

/** Chỉ access token mới được dùng để gọi API. Refresh token bị từ chối. */
public class TokenTypeValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR =
            new OAuth2Error("invalid_token", "Only access tokens are accepted", null);

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        return JwtService.TYPE_ACCESS.equals(token.getClaimAsString(JwtService.CLAIM_TOKEN_TYPE))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(ERROR);
    }
}