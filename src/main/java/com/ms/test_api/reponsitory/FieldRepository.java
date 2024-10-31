package com.ms.test_api.reponsitory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms.test_api.model.Field;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer>{
    Optional<Field> findByFieldId(int fieldId);
}
