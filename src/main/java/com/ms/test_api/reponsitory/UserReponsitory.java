package com.ms.test_api.reponsitory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.model.UserSoccerField;

@Repository
public interface UserReponsitory extends JpaRepository<UserSoccerField, String>{
    
    Optional<UserSoccerField> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByCCCD(String CCCD);

    boolean existsByEmail(String email);

}
