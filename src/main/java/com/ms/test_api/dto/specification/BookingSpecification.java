package com.ms.test_api.dto.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ms.test_api.modal.Booking;

import jakarta.persistence.criteria.Predicate;

public class BookingSpecification {

    public static Specification<Booking> filterBookings(Integer userId, String branchName, String userName, Boolean status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
    
            if (userId != 0) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("userId"), userId));
            }
            if (branchName != null && !branchName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("field").get("branch").get("branchName"), "%" + branchName + "%"));
            }
            if (userName != null && !userName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("user").get("username"), "%" + userName + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
    
            return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    

}
