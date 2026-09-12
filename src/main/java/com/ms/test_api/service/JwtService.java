package com.ms.test_api.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ms.test_api.config.JwtProperties;
import com.ms.test_api.entity.User;
import com.ms.test_api.exception.UnauthorizedException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Instant expiresAt = Instant.now().plus(jwtProperties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);

        JWTClaimsSet claims = baseClaims(user, TYPE_ACCESS, expiresAt)
                .claim("scope", user.getRole().getName().name())
                .claim("userId", user.getId())
                .build();

        return sign(claims);
    }

    public String generateRefreshToken(User user) {
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtlDays(), ChronoUnit.DAYS);

        // Refresh token KHÔNG chứa scope: nó không dùng để phân quyền,
        // và nếu role đổi thì access token mới phải lấy role mới từ DB.
        return sign(baseClaims(user, TYPE_REFRESH, expiresAt).build());
    }

    public SignedJWT parseAndVerify(String token, String expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(new MACVerifier(jwtProperties.signerKey().getBytes()))) {
                throw new UnauthorizedException("Token signature is invalid");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new UnauthorizedException("Token is expired");
            }

            if (!expectedType.equals(claims.getStringClaim(CLAIM_TOKEN_TYPE))) {
                throw new UnauthorizedException("Wrong token type");
            }

            if (claims.getJWTID() == null) {
                throw new UnauthorizedException("Token has no identifier");
            }

            return signedJWT;

        } catch (ParseException | JOSEException e) {
            log.debug("Token could not be parsed or verified", e);
            throw new UnauthorizedException("Token is malformed");
        }
    }

    public String extractTokenId(SignedJWT signedJWT) {
        return readClaims(signedJWT).getJWTID();
    }

    public String extractSubject(SignedJWT signedJWT) {
        return readClaims(signedJWT).getSubject();
    }

    public Instant extractExpiresAt(SignedJWT signedJWT) {
        return readClaims(signedJWT).getExpirationTime().toInstant();
    }

    private JWTClaimsSet readClaims(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new UnauthorizedException("Token is malformed");
        }
    }

    private JWTClaimsSet.Builder baseClaims(User user, String tokenType, Instant expiresAt) {
        return new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .issuer(jwtProperties.issuer())
                .issueTime(new Date())
                .expirationTime(Date.from(expiresAt))
                .claim(CLAIM_TOKEN_TYPE, tokenType);
    }

    private String sign(JWTClaimsSet claims) {
        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS512),
                new Payload(claims.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(jwtProperties.signerKey().getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot sign JWT", e);
            throw new IllegalStateException("Cannot create token", e);
        }
    }
}
