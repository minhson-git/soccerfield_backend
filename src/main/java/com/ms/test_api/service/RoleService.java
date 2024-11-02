package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.model.Role;

public interface RoleService {

    List<RoleDTO> getAllRoles();

    Role addRole(Role role);

    ResponseEntity<RoleDTO> getRoleById(int id);

    ResponseEntity<Role> updateRole(int id, Role role);

    ResponseEntity<?> deleteRole(int id);

}   
