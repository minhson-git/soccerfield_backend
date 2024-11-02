package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.model.Role;
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
    public ResponseEntity<RoleDTO> getRoleById(int id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not exist with id: " + id));
        RoleDTO roleDTOs = new RoleDTO(role.getId(), role.getName());
        return ResponseEntity.ok(roleDTOs);
    }

    @Override
    public ResponseEntity<Role> updateRole(int id, Role roleDetails) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not exist with id: " + id));

        role.setId(roleDetails.getId());
        role.setName(roleDetails.getName());

        Role updateRole = roleRepository.save(role);
        return ResponseEntity.ok(updateRole);
    }

    @Override
    public ResponseEntity<?> deleteRole(int id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not exist with id: " + id));
        roleRepository.delete(role);
        return ResponseEntity.ok().build();
    }

}
