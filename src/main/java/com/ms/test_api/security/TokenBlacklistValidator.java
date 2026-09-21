package com.ms.test_api.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.ms.test_api.service.TokenBlacklistService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenBlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR =
            new OAuth2Error("invalid_token", "Token has been revoked", null);

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tokenId = token.getId();

        if (tokenId == null || tokenBlacklistService.isBlacklisted(tokenId)) {
            return OAuth2TokenValidatorResult.failure(ERROR);
        }
        return OAuth2TokenValidatorResult.success();
    }
}