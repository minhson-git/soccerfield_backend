package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Role;

public interface RoleService {

    List<RoleDTO> getAllRoles();

    Role addRole(Role role);

    ResponseEntity<ApiResponse<RoleDTO>> getRoleById(int id);

    ResponseEntity<ApiResponse<Role>> updateRole(int id, Role role);

    ResponseEntity<?> deleteRole(int id);

}   
