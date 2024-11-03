package com.ms.test_api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ms.test_api.dto.UserDTO;
import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.response.ApiResponse;
import com.ms.test_api.model.UserSoccerField;

public interface UserService {

    UserDetailsService userDetailsService();

    List<UserDTO> getAllUsers();

    ResponseEntity<ApiResponse<UserSoccerField>> registerUser(UserCreationRequest user);

    ResponseEntity<UserDTO> getUserByUsername(String username);

    ResponseEntity<UserSoccerField> updateUserByUsername(String username, UserSoccerField user);

    ResponseEntity<?> deleteUser(String username);

}
