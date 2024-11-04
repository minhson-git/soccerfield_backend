package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.Role;
import com.ms.test_api.reponsitory.RoleRepository;
import com.ms.test_api.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService{

    private final RoleRepository roleRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
            .map(role -> new RoleDTO(role.getId(), role.getName())).collect(Collectors.toList());
    }

    @Override
    public Role addRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(int id) {
        try {
            Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not exist with id: " + id));
            RoleDTO roleDTOs = new RoleDTO(role.getId(), role.getName());
            
            ApiResponse<RoleDTO> response = new ApiResponse<RoleDTO>(
                "Successfully retrieved role data", 
                HttpStatus.OK.value(), 
                roleDTOs
            );
            return new ResponseEntity<ApiResponse<RoleDTO>>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<RoleDTO> response = new ApiResponse<RoleDTO>(
                "Failed retrieved role data", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<ApiResponse<RoleDTO>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    public ResponseEntity<ApiResponse<Role>> updateRole(int id, Role roleDetails) {
        try {            
            Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not exist with id: " + id));
    
            role.setId(role.getId());
            role.setName(roleDetails.getName());
    
            roleRepository.save(role);
            ApiResponse<Role> response = new ApiResponse<Role>(
                "Updated role sucessfully", 
                HttpStatus.OK.value(), 
                null
            );
            return new ResponseEntity<ApiResponse<Role>>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Role> response = new ApiResponse<Role>(
                "Failed to update role", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<ApiResponse<Role>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @Override
    public ResponseEntity<?> deleteRole(int id) {
        try {
            Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not exist with id: " + id));
            roleRepository.delete(role);
            ApiResponse<String> response = new ApiResponse<String>(
                "Delete role sucessfully", 
                HttpStatus.OK.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<String>(
                "Failed to delete role", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
