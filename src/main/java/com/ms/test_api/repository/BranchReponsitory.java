package com.ms.test_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.entity.Branch;

@Repository
public interface BranchReponsitory extends JpaRepository<Branch, Integer>{

}
