package com.ms.test_api.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.config.JwtProperties;
import com.ms.test_api.dto.request.LogoutRequest;
import com.ms.test_api.dto.request.RefreshRequest;
import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.AuthenticationResponse;
import com.ms.test_api.entity.User;
import com.ms.test_api.exception.UnauthorizedException;
import com.ms.test_api.repository.UserRepository;
import com.ms.test_api.util.PhoneNumbers;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    private Optional<User> findByIdentifier(String identifier) {
        String normalizedPhone = PhoneNumbers.normalizeVietnamese(identifier);

        return normalizedPhone != null
                ? userRepository.findByPhone(normalizedPhone)
                : userRepository.findByUsername(identifier.trim());
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(SignInRequest request) {
        User user = findByIdentifier(request.identifier())
                .orElseThrow(() -> new UnauthorizedException("Username or password is incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Username or password is incorrect");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("Account is disabled");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthenticationResponse refresh(RefreshRequest request) {

        SignedJWT refreshToken = jwtService.parseAndVerify(request.refreshToken(), JwtService.TYPE_REFRESH);
        String tokenId = jwtService.extractTokenId(refreshToken);

        if (tokenBlacklistService.isBlacklisted(tokenId)) {
            log.warn("Attempt to reuse a revoked refresh token: {}", tokenId);
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        User user = userRepository.findByUsername(jwtService.extractSubject(refreshToken))
                .orElseThrow(() -> new UnauthorizedException("Token subject no longer exists"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("Account is disabled");
        }

        // Rotation: refresh token cũ chỉ dùng được đúng một lần.
        tokenBlacklistService.blacklist(tokenId, jwtService.extractExpiresAt(refreshToken));

        return issueTokens(user);
    }

    @Transactional
    public void logout(Jwt accessToken, LogoutRequest request) {

        if (accessToken.getId() != null && accessToken.getExpiresAt() != null) {
            tokenBlacklistService.blacklist(accessToken.getId(), accessToken.getExpiresAt());
        }

        // Refresh token hỏng hoặc hết hạn không nên làm logout thất bại —
        // mục tiêu của user đã đạt được: token cũ không dùng được nữa.
        try {
            SignedJWT refreshToken = jwtService.parseAndVerify(request.refreshToken(), JwtService.TYPE_REFRESH);
            tokenBlacklistService.blacklist(
                    jwtService.extractTokenId(refreshToken),
                    jwtService.extractExpiresAt(refreshToken));
        } catch (UnauthorizedException e) {
            log.debug("Refresh token supplied at logout was already invalid: {}", e.getMessage());
        }
    }

    private AuthenticationResponse issueTokens(User user) {
        return new AuthenticationResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtProperties.accessTokenTtlMinutes() * 60,
                user.getRole().getName(),
                user.getId());
    }
}