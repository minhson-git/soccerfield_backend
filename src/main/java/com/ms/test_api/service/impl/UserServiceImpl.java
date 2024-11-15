package com.ms.test_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ms.test_api.dto.RoleDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.exception.UserNotFoundException;
import com.ms.test_api.modal.Role;
import com.ms.test_api.modal.UserSoccerField;
import com.ms.test_api.reponsitory.RoleRepository;
import com.ms.test_api.reponsitory.UserReponsitory;
import com.ms.test_api.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserReponsitory userReponsitory;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userReponsitory.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserSoccerField> users = userReponsitory.findAll();
        return users.stream()
            .map(user -> new UserDTO(
                user.getUserId(),
                user.getCitizenId(), 
                user.getUsername(), 
                user.getEmail(), 
                user.getFullname(), 
                user.getPhone(), 
                new RoleDTO(user.getRole().getId(), user.getRole().getName())
                )).collect(Collectors.toList());
        
    }

    @Override
    public ResponseEntity<ApiResponse<UserSoccerField>> registerUser(UserCreationRequest user) {

        if(userReponsitory.existsByUsername(user.getUsername()) || 
        userReponsitory.existsByCitizenId(user.getCitizenId()) || 
        userReponsitory.existsByEmail(user.getEmail())){
            log.error("User with provided username, citizenId, or email already exists");
            throw new RuntimeException("User existed or CitizenId existed or Email existed");
        } 
        try {
            UserSoccerField newUser = new UserSoccerField();
            newUser.setCitizenId(user.getCitizenId());
            newUser.setUsername(user.getUsername());
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
            newUser.setEmail(user.getEmail());
            newUser.setFullname(user.getFullname());
            newUser.setPhone(user.getPhone());

            Role role = roleRepository.findByName("user").orElseThrow(()-> new RuntimeException("Role not found"));
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
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(String username) {
        try {     
            UserSoccerField user = userReponsitory.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not exist with username: "+ username));
            UserDTO userDTOs = new UserDTO(
                user.getUserId(),
                user.getCitizenId(), 
                user.getUsername(), 
                user.getEmail(), 
                user.getFullname(), 
                user.getPhone(), 
                new RoleDTO(user.getRole().getId(), user.getRole().getName()));
            
            ApiResponse<UserDTO> response = new ApiResponse<UserDTO>(
                "Successfully retrieved user data", 
                HttpStatus.OK.value(), 
                userDTOs
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = new ApiResponse<UserDTO>(
                "Failed retrieved user data", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UserDTO>> updateUserByUsername(int id, UserSoccerField userDetail) {
        
        try {
            UserSoccerField user = userReponsitory.findById(id).orElseThrow(() -> new UserNotFoundException("User not exist with id: " + id));
    
            user.setUserId(user.getUserId());
            user.setCitizenId(userDetail.getCitizenId());
            user.setEmail(userDetail.getEmail());
            user.setFullname(userDetail.getFullname());
            user.setUsername(userDetail.getUsername());
            user.setPassword(user.getPassword());
            user.setPhone(userDetail.getPhone());
            
            Role role = roleRepository.findById(user.getRole().getId()).orElseThrow(()-> new RuntimeException("Role not found"));
            user.setRole(role);
            
            userReponsitory.save(user);

            UserDTO userDTOs = new UserDTO(
                user.getUserId(),
                user.getCitizenId(), 
                user.getUsername(), 
                user.getEmail(), 
                user.getFullname(), 
                user.getPhone(), 
                new RoleDTO(user.getRole().getId(), user.getRole().getName()));

            ApiResponse<UserDTO> response = new ApiResponse<UserDTO>(
                "Updated successfully user", 
                HttpStatus.OK.value(), 
                userDTOs
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<UserDTO> response = new ApiResponse<UserDTO>(
                "Failed retrieved user data", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteUser(int id) {
        try {
            UserSoccerField user = userReponsitory.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not exist with id: " + id));
    
            userReponsitory.delete(user);
            ApiResponse<String> response = new ApiResponse<String>(
                "Deleted successfully user", 
                HttpStatus.OK.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<String>(
                "Failed to delete user", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
