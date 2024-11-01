package com.ms.test_api.service;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.TokenResponse;
import com.ms.test_api.model.UserSoccerField;
import com.ms.test_api.reponsitory.UserReponsitory;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserReponsitory userReponsitory;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public TokenResponse authenticate(SignInRequest signInRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        var user = userReponsitory.findByUsername(signInRequest.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Username or password incorrect"));

        String accessToken = jwtService.generateToken(user);

        // String refreshToken = jwtService.generateRefreshToken(user);

        return TokenResponse.builder()
                .message("Login Successfully!")
                .accessToken(accessToken)
                .userID(user.getCCCD())
                .role(user.getRole().getName())
                .build();
    }

    // public TokenResponse refresh(HttpServletRequest request) {
    //     String refreshToken = request.getHeader("x-token");
    //     if(StringUtils.isBlank(refreshToken)){
    //         throw new InvalidMetadataException("Token must be not blank");
    //     }

    //     final String userName = jwtService.extractUsername(refreshToken);

    //     Optional<UserSoccerField> user = userReponsitory.findByUsername(userName);

    //     if(!jwtService.isValid(refreshToken, user.get())){
    //         throw new InvalidMetadataException("Token is invalid");
    //     }

    //     String accessToken = jwtService.generateToken(user.get());


    //     return TokenResponse.builder()
    //             .accessToken(accessToken)
    //             .refreshToken(refreshToken)
    //             .userCCCD(user.get().getCCCD())
    //             .fullName(user.get().getFullname())
    //             .build();
    // }
}
