package com.ms.test_api.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.ms.test_api.entity.Field;

import jakarta.persistence.criteria.Predicate;

public final class FieldSpecification {

    private FieldSpecification() {
    }

    public static Specification<Field> filter(FieldFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.branchName())) {
                predicates.add(cb.like(root.get("branch").get("name"), "%" + filter.branchName() + "%"));
            }
            if (StringUtils.hasText(filter.district())) {
                predicates.add(cb.equal(root.get("branch").get("district"), filter.district()));
            }
            if (filter.fieldType() != null) {
                predicates.add(cb.equal(root.get("fieldType"), filter.fieldType()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), filter.maxPrice()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}