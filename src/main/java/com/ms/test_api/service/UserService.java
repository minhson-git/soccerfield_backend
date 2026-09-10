package com.ms.test_api.service;

import java.util.List;

import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.request.UserUpdateRequest;
import com.ms.test_api.dto.response.UserResponse;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse register(UserCreationRequest request);

    UserResponse getByUsername(String username);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}