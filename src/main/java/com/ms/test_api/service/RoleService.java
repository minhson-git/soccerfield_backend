package com.ms.test_api.service;

import java.util.List;

import com.ms.test_api.dto.response.RoleResponse;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Long id);
}