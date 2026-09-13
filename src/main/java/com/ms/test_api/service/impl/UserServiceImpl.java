package com.ms.test_api.service.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.dto.request.UserCreationRequest;
import com.ms.test_api.dto.request.UserUpdateRequest;
import com.ms.test_api.dto.response.UserResponse;
import com.ms.test_api.entity.Role;
import com.ms.test_api.entity.User;
import com.ms.test_api.entity.enums.RoleName;
import com.ms.test_api.exception.BadRequestException;
import com.ms.test_api.exception.ConflictException;
import com.ms.test_api.exception.ResourceNotFoundException;
import com.ms.test_api.mapper.UserMapper;
import com.ms.test_api.repository.BranchRepository;
import com.ms.test_api.repository.RoleRepository;
import com.ms.test_api.repository.UserRepository;
import com.ms.test_api.service.UserService;
import com.ms.test_api.util.PhoneNumbers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private String requireValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        if (PhoneNumbers.normalizeVietnamese(phone) == null) {
            throw new BadRequestException("Invalid phone number format: " + phone);
        }

        return PhoneNumbers.normalizeVietnamese(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse register(UserCreationRequest request) {

        String normalizedPhone = requireValidPhone(request.phone());

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists: " + request.email());
        }
        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new ConflictException("Phone number already exists: " + request.phone());
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Default role CUSTOMER is not configured"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(normalizedPhone);
        user.setEnabled(true);
        user.setRole(customerRole);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    private UserResponse updateProfile(User user, UserUpdateRequest request) {
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists: " + request.email());
        }

        String normalizedPhone = requireValidPhone(request.phone());
        if (normalizedPhone == null && user.getPhone() != null) {
            throw new BadRequestException("Phone number cannot be empty");
        }
        if (!user.getPhone().equals(normalizedPhone) && userRepository.existsByPhone(normalizedPhone)) {
            throw new ConflictException("Phone number already exists: " + request.phone());
        }

        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(normalizedPhone);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return updateProfile(user, request);
    }

    @Override
    @Transactional
    public UserResponse updateOwnProfile(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return updateProfile(user, request);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(Long id, RoleName roleName) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " is not configured"));

        if (user.getRole().getName() == RoleName.OWNER && roleName != RoleName.OWNER
                && !branchRepository.findByOwner_Username(user.getUsername()).isEmpty()) {
            throw new ConflictException(
                    "User still owns branches; reassign them before removing the OWNER role");
        }

        user.setRole(role);

        // Xem §10.2: access token đang lưu hành vẫn mang scope cũ tới khi hết hạn.
        log.warn("Role of user {} changed to {}; existing access tokens keep the old scope ", user.getUsername(),
                roleName);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}