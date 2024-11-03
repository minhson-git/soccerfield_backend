package com.ms.test_api.controller;

import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.request.IntrospectRequest;
import com.ms.test_api.dto.request.SignInRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.dto.response.IntrospectResponse;
import com.ms.test_api.dto.response.TokenResponse;
import com.ms.test_api.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
@Validated
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> authenticate(@RequestBody SignInRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<TokenResponse>builder()
                .message("Login Succesfully")
                .statusCode(HttpStatus.OK.value())
                .data(result)
                .build();
    }


    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) 
            throws JOSEException, ParseException{
        var result = authenticationService.introspectResponse(request);
        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(result)
                .build();
    }



}
