package com.ms.test_api.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(

        @NotEmpty(message = "At least one allowed origin must be configured")
        List<String> allowedOrigins,

        @DefaultValue({"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"})
        List<String> allowedMethods,

        @DefaultValue("*")
        List<String> allowedHeaders,

        @DefaultValue("true")
        boolean allowCredentials,

        @Positive
        @DefaultValue("3600")
        long maxAge
) {

    public CorsProperties {
        if (allowCredentials && allowedOrigins != null && allowedOrigins.contains("*")) {
            throw new IllegalArgumentException(
                    "app.cors.allow-credentials=true cannot be combined with allowed-origins=*. "
                            + "List the origins explicitly.");
        }
    }
}