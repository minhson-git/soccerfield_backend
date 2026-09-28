package com.ms.test_api.mapper;

import org.springframework.stereotype.Component;

import com.ms.test_api.dto.response.RoleResponse;
import com.ms.test_api.dto.response.UserResponse;
import com.ms.test_api.dto.response.UserSummaryResponse;
import com.ms.test_api.entity.Role;
import com.ms.test_api.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getEnabled(),
                toRoleResponse(user.getRole()));
    }

    public UserSummaryResponse toSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getFullName());
    }

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleResponse(role.getId(), role.getName());
    }
}