package com.ms.test_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.ms.test_api.entity.Field;

public interface FieldRepository extends JpaRepository<Field, Long>, JpaSpecificationExecutor<Field> {
    @Override
    @EntityGraph(attributePaths = { "branch" })
    Page<Field> findAll(Specification<Field> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "branch" })
    Optional<Field> findById(Long id);

}