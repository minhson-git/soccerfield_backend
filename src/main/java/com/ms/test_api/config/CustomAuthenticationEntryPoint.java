package com.ms.test_api.config;

import java.io.IOException;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.test_api.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.of(
                resolveMessage(authException),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                null);

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * Phân biệt các lý do để client biết nên làm gì tiếp: hết hạn thì gọi /auth/refresh,
     * bị thu hồi thì đăng nhập lại. Mô tả lỗi đến từ các validator trong SecurityConfig.
     */
    private static String resolveMessage(AuthenticationException ex) {
        if (!(ex instanceof OAuth2AuthenticationException)) {
            return "Authentication is required to access this resource";
        }

        String detail = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);

        if (detail.contains("expired")) {
            return "Access token has expired, please refresh it";
        }
        if (detail.contains("revoked")) {
            return "Access token has been revoked, please log in again";
        }
        if (detail.contains("only access tokens")) {
            return "Refresh token cannot be used to call the API, send the access token instead";
        }
        if (detail.contains("malformed") && detail.contains("bearer")) {
            return "Authorization header is malformed, expected 'Bearer <token>'";
        }
        return "Access token is invalid";
    }
}