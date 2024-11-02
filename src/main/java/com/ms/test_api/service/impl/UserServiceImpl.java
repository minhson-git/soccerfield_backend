package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.exception.UserNotFoundException;
import com.ms.test_api.model.Role;
import com.ms.test_api.model.UserSoccerField;
import com.ms.test_api.reponsitory.RoleRepository;
import com.ms.test_api.reponsitory.UserReponsitory;
import com.ms.test_api.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserReponsitory userReponsitory;

    private final RoleRepository roleRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userReponsitory.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserSoccerField> users = userReponsitory.findAll();
        return users.stream()
            .map(user -> new UserDTO(
                user.getCCCD(), 
                user.getUsername(), 
                user.getPassword(), 
                user.getEmail(), 
                user.getFullname(), 
                user.getPhone(), 
                new RoleDTO(user.getRole().getId(), user.getRole().getName())
                )).collect(Collectors.toList());
    }

    @Bean
    public PasswordEncoder setPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserSoccerField registerUser(UserSoccerField user) {
        user.setPassword(setPasswordEncoder().encode(user.getPassword()));
        Role role = roleRepository.findById(1).orElseThrow(()-> new ResourceNotFoundException("Role not found"));
        user.setRole(role);
        return userReponsitory.save(user);
    }

    @Override
    public ResponseEntity<UserDTO> getUserByUsername(String username) {
        UserSoccerField user = userReponsitory.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not exist with username: "+ username));
        UserDTO userDTOs = new UserDTO(
            user.getCCCD(), 
            user.getUsername(), 
            user.getPassword(), 
            user.getEmail(), 
            user.getFullname(), 
            user.getPhone(), 
            new RoleDTO(user.getRole().getId(), user.getRole().getName()));
        
        return ResponseEntity.ok(userDTOs);
    }

    @Override
    public ResponseEntity<UserSoccerField> updateUserByUsername(String username, UserSoccerField userDetail) {
        UserSoccerField user = userReponsitory.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not exist with username: " + username));

        user.setCCCD(userDetail.getCCCD());
        user.setEmail(userDetail.getEmail());
        user.setFullname(userDetail.getFullname());
        user.setUsername(userDetail.getUsername());
        user.setPassword(setPasswordEncoder().encode(userDetail.getPassword()));
        user.setPhone(userDetail.getPhone());
        user.setRole(userDetail.getRole());

        UserSoccerField updateUser = userReponsitory.save(user);
        return ResponseEntity.ok(updateUser);
    }

    @Override
    public ResponseEntity<?> deleteUser(String username) {
        UserSoccerField user = userReponsitory.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not exist with username: " + username));

        userReponsitory.delete(user);
        return ResponseEntity.ok().build();
    }
}
