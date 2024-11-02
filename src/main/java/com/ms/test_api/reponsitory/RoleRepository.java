package com.ms.test_api.reponsitory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ms.test_api.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

}
