package com.ms.test_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.request.LogoutRequest;
import com.ms.test_api.dto.request.RefreshRequest;
import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.response.AuthenticationResponse;
import com.ms.test_api.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @RequestBody @Valid SignInRequest request) {
        return ApiResponse.ok("Login successfully", authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
            @RequestBody @Valid RefreshRequest request) {
        return ApiResponse.ok("Token refreshed successfully", authenticationService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Jwt accessToken,
            @RequestBody @Valid LogoutRequest request) {
        authenticationService.logout(accessToken, request);
        return ApiResponse.ok("Logged out successfully", null);
    }
}