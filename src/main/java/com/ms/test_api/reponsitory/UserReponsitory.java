package com.ms.test_api.reponsitory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.modal.UserSoccerField;

@Repository
public interface UserReponsitory extends JpaRepository<UserSoccerField, Integer>{
    
    Optional<UserSoccerField> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByCitizenId(String citizenId);

    boolean existsByEmail(String email);

}
