package com.ms.test_api.dto.response;

import com.ms.test_api.entity.enums.RoleName;

public record RoleResponse(Long id, RoleName name) {}