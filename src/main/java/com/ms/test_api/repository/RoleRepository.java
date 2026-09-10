package com.ms.test_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ms.test_api.entity.Role;
import com.ms.test_api.entity.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}