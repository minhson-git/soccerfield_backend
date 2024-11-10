package com.ms.test_api.dto.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ms.test_api.modal.Field;

import jakarta.persistence.criteria.Predicate;

public class FieldSpecification {
     public static Specification<Field> filterFields(String branchName, String fieldType, Boolean status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (branchName != null && !branchName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("branch").get("branchName"), "%" + branchName + "%"));
            }

            if (fieldType != null && !fieldType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("fieldType"), fieldType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
