package com.ms.test_api.service;

import java.util.List;

import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.request.UserUpdateRequest;
import com.ms.test_api.dto.response.UserResponse;
import com.ms.test_api.entity.enums.RoleName;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse register(UserCreationRequest request);

    UserResponse getByUsername(String username);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse updateOwnProfile(String username, UserUpdateRequest request);

    UserResponse changeRole(Long id, RoleName roleName);
}