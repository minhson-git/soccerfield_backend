package com.ms.test_api.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.dto.request.IntrospectRequest;
import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.IntrospectResponse;
import com.ms.test_api.dto.response.TokenResponse;
import com.ms.test_api.entity.User;
import com.ms.test_api.exception.UnauthorizedException;
import com.ms.test_api.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
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
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.signer-key}")
    private String signerKey;

    @Value("${jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    @Transactional(readOnly = true)
    public TokenResponse authenticate(SignInRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Username or password is incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Username or password is incorrect");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("Account is disabled");
        }

        return new TokenResponse(
                true,
                generateToken(user),
                user.getRole().getName(),
                user.getId());
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(request.token());
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

            boolean signatureValid = signedJWT.verify(verifier);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            return new IntrospectResponse(signatureValid && expiryTime != null && expiryTime.after(new Date()));
        } catch (JOSEException | ParseException e) {
            log.debug("Token introspection failed", e);
            return new IntrospectResponse(false);
        }
    }

    private String generateToken(User user) {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("minhson.dev")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(accessTokenTtlMinutes, ChronoUnit.MINUTES)))
                .claim("userId", user.getId())
                .claim("scope", user.getRole().getName().name())
                .build();

        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS512),
                new Payload(claimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token for user {}", user.getUsername(), e);
            throw new IllegalStateException("Cannot create token", e);
        }
    }
}