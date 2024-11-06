package com.ms.test_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Role;
import com.ms.test_api.service.impl.RoleServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleServiceImpl roleServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles(){
        try {
            List<RoleDTO> roles = roleServiceImpl.getAllRoles();
            ApiResponse<List<RoleDTO>> response = new ApiResponse<>(
                "Successfully retrieved role data",
                HttpStatus.OK.value(),
                roles
            );
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            // TODO: handle exception
            ApiResponse<List<RoleDTO>> response = new ApiResponse<>(
                "Failed to retrieve role data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> addRole(@RequestBody Role role){
        try {
            roleServiceImpl.addRole(role);
            ApiResponse<Role> response = new ApiResponse<Role>(
                "Role created successfully", 
                HttpStatus.CREATED.value(), 
                null
            ); 
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Role> response = new ApiResponse<>(
                "Failed to create role",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(@PathVariable int id){
        return roleServiceImpl.getRoleById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable int id, @RequestBody Role role){
        return roleServiceImpl.updateRole(id, role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable int id){
        return roleServiceImpl.deleteRole(id);
    }

}
