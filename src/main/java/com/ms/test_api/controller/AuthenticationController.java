package com.ms.test_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.request.IntrospectRequest;
import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.response.IntrospectResponse;
import com.ms.test_api.dto.response.TokenResponse;
import com.ms.test_api.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid SignInRequest request) {
        return ApiResponse.ok("Login successfully", authenticationService.authenticate(request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody @Valid IntrospectRequest request) {
        return ApiResponse.ok("Token introspected", authenticationService.introspect(request));
    }

    // TODO (Day 4): POST /refresh, POST /logout
}