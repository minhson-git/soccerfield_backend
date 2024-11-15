package com.ms.test_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms.test_api.dto.BranchDTO;
import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.modal.UserSoccerField;
import com.ms.test_api.service.impl.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(){
        try {
            List<UserDTO> userDTOs = userServiceImpl.getAllUsers();
            ApiResponse<List<UserDTO>> response = new ApiResponse<List<UserDTO>>(
                "Successfully retrieved user data", 
                HttpStatus.OK.value(), 
                userDTOs
            );
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<List<BranchDTO>> response = new ApiResponse<>(
                "Failed to retrieve user data",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
            );
            return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserSoccerField>> registerUser(@RequestBody @Valid UserCreationRequest user){
        return userServiceImpl.registerUser(user);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username){
        return userServiceImpl.getUserByUsername(username);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable int id, @RequestBody UserSoccerField user){
        return userServiceImpl.updateUserByUsername(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id){
        return userServiceImpl.deleteUser(id);
    }
    

}
