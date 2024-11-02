package com.ms.test_api.controller;

import java.util.List;

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
import com.ms.test_api.model.Role;
import com.ms.test_api.service.impl.RoleServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleServiceImpl roleServiceImpl;

    @GetMapping
    public List<RoleDTO> getAllRoles(){
        return roleServiceImpl.getAllRoles();
    }

    @PostMapping
    public Role addRole(Role role){
        return roleServiceImpl.addRole(role);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable int id){
        return roleServiceImpl.getRoleById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable int id, @RequestBody Role role){
        return roleServiceImpl.updateRole(id, role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable int id){
        return roleServiceImpl.deleteRole(id);
    }

}
