package com.ms.test_api.reponsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.modal.Branch;

@Repository
public interface BranchReponsitory extends JpaRepository<Branch, Integer>{

}
