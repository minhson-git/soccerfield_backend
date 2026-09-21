package com.ms.test_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.ms.test_api.entity.Field;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface FieldRepository extends JpaRepository<Field, Long>, JpaSpecificationExecutor<Field> {
    @Override
    @EntityGraph(attributePaths = { "branch" })
    Page<Field> findAll(Specification<Field> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "branch" })
    Optional<Field> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT f FROM Field f WHERE f.id = :id")
    Optional<Field> findByIdForUpdate(@Param("id") Long id);
}