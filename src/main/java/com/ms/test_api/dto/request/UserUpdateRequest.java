package com.ms.test_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @NotBlank @Email @Size(max = 100)
        String email,

        @NotBlank @Size(max = 100)
        String fullName,

        @Size(max = 20)
        String phone
) {}