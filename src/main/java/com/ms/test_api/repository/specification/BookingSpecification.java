package com.ms.test_api.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.ms.test_api.entity.Booking;

import jakarta.persistence.criteria.Predicate;

public final class BookingSpecification {

    private BookingSpecification() {
    }

    public static Specification<Booking> filter(BookingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }
            if (StringUtils.hasText(filter.username())) {
                predicates.add(cb.like(root.get("user").get("username"), "%" + filter.username() + "%"));
            }
            if (StringUtils.hasText(filter.branchName())) {
                predicates.add(cb.like(root.get("field").get("branch").get("name"), "%" + filter.branchName() + "%"));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.bookingDate() != null) {
                predicates.add(cb.equal(root.get("bookingDate"), filter.bookingDate()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}