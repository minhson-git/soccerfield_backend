package com.ms.test_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ms.test_api.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}