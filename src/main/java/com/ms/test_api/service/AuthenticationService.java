package com.ms.test_api.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ms.test_api.dto.request.IntrospectRequest;
import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.IntrospectResponse;
import com.ms.test_api.dto.response.TokenResponse;
import com.ms.test_api.modal.UserSoccerField;
import com.ms.test_api.reponsitory.UserReponsitory;
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
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserReponsitory userReponsitory;

    @NonFinal
    protected static final String SIGNER_KEY = "5q4o2IvLashcRxzV+SpTRVhbcgTdvyYfdijgpGC8tTOFa3agKANLtoM9D4mNOHeF";

    public IntrospectResponse introspectResponse(IntrospectRequest request)
            throws JOSEException, ParseException{
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirtyTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verifed = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verifed && expirtyTime.after(new Date()))
                .build();
            
    }

    public TokenResponse authenticate(SignInRequest request){
        var user = userReponsitory.findByUsername(request.getUsername()).orElseThrow(()-> new RuntimeException("User not exist"));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated){
            throw new RuntimeException("Username or password incorrect");
        }

        var token = generateToken(user);

        return TokenResponse.builder()
                .token(token)
                .authenticated(authenticated)
                .role(user.getRole().getName())
                .userId(user.getUserId())
                .build();
    }
    
    private String generateToken (UserSoccerField user){

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("minhson.dev")
                    .issueTime(new Date())
                    .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                    ))
                    .claim("scope", user.getRole().getName().toUpperCase())
                    .build();
        
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }
}
