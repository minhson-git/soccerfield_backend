package com.ms.test_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ms.test_api.entity.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long> {
}