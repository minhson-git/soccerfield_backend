package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<UserSoccerField>> registerUser(UserCreationRequest user) {

        if(userReponsitory.existsByUsername(user.getUsername()) || userReponsitory.existsByCCCD(user.getCccd()) || userReponsitory.existsByEmail(user.getEmail())){
            throw new RuntimeException("User existed or UserID existed or Email existed");
        } 
        try {
            UserSoccerField newUser = new UserSoccerField();
            newUser.setCCCD(user.getCccd());
            newUser.setUsername(user.getUsername());
            newUser.setPassword(setPasswordEncoder().encode(user.getPassword()));
            newUser.setEmail(user.getEmail());
            newUser.setFullname(user.getFullname());
            newUser.setPhone(user.getPhone());

            Role role = roleRepository.findById(1).orElseThrow(()-> new RuntimeException("Role not found"));
            newUser.setRole(role);
        
            userReponsitory.save(newUser);

            ApiResponse<UserSoccerField> response = new ApiResponse<UserSoccerField>(
                "User registered successfully", 
                HttpStatus.CREATED.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<UserSoccerField> response = new ApiResponse<UserSoccerField>(
                "Failed to registration user", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
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
