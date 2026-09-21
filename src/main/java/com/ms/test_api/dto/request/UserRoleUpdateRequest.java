package com.ms.test_api.dto.request;

import com.ms.test_api.entity.enums.RoleName;

import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(

        @NotNull(message = "Role is required")
        RoleName role
) {}