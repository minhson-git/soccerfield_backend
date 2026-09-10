package com.ms.test_api.dto.response;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        Boolean enabled,
        RoleResponse role
) {}