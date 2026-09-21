package com.ms.test_api.mapper;

import org.springframework.stereotype.Component;

import com.ms.test_api.dto.request.BranchRequest;
import com.ms.test_api.dto.response.BranchResponse;
import com.ms.test_api.entity.Branch;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        if (branch == null) {
            return null;
        }
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                branch.getDistrict(),
                branch.getPhone(),
                branch.getOpeningTime(),
                branch.getClosingTime());
    }

    public void applyRequest(Branch branch, BranchRequest request) {
        branch.setName(request.name());
        branch.setAddress(request.address());
        branch.setDistrict(request.district());
        branch.setPhone(request.phone());
        branch.setOpeningTime(request.openingTime());
        branch.setClosingTime(request.closingTime());
    }
}
