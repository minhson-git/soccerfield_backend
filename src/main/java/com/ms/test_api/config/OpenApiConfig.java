package com.ms.test_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    OpenAPI soccerFieldOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Soccer Field Booking API")
                        .version("v1")
                        .description("""
                                Mọi response đều bọc trong ApiResponse: message, statusCode, timestamp, path, data.
                                Dữ liệu thật nằm ở trường `data`.

                                Xác thực: POST /api/v1/auth/login, lấy accessToken, bấm Authorize ở trên.
                                Refresh token xoay vòng — token cũ chết ngay sau khi gọi /auth/refresh.
                                """))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}