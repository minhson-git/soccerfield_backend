package com.ms.test_api.security;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ms.test_api.entity.Branch;
import com.ms.test_api.repository.BookingRepository;
import com.ms.test_api.repository.BranchRepository;
import com.ms.test_api.repository.FieldRepository;

import lombok.RequiredArgsConstructor;

/**
 * Kiểm tra quyền sở hữu, dùng trong SpEL của @PreAuthorize.
 * Ví dụ: @PreAuthorize("hasRole('ADMIN') or @branchSecurity.ownsField(#id, authentication.name)")
 */
@Component("branchSecurity")
@RequiredArgsConstructor
public class BranchSecurity {

    private final BranchRepository branchRepository;
    private final FieldRepository fieldRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public boolean ownsBranch(Long branchId, String username) {
        if (branchId == null || username == null) {
            return false;
        }
        return branchRepository.findById(branchId)
                .map(Branch::getOwner)
                .map(owner -> owner.getUsername().equals(username))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ownsField(Long fieldId, String username) {
        if (fieldId == null || username == null) {
            return false;
        }
        return fieldRepository.findById(fieldId)
                .map(field -> field.getBranch())
                .map(Branch::getOwner)
                .map(owner -> owner.getUsername().equals(username))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ownsBookedField(Long bookingId, String username) {
        if (bookingId == null || username == null) {
            return false;
        }
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getField().getBranch())
                .map(Branch::getOwner)
                .map(owner -> owner.getUsername().equals(username))
                .orElse(false);
    }
}