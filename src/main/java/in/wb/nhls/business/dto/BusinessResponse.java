package in.wb.nhls.business.dto;

import in.wb.nhls.business.entity.Business;
import in.wb.nhls.business.entity.BusinessType;

public record BusinessResponse(
        Long id,
        String businessName,
        BusinessType businessType,
        String ownerName,
        String district) {

    public static BusinessResponse fromEntity(Business business) {

        return new BusinessResponse(
                business.getId(),
                business.getBusinessName(),
                business.getBusinessType(),
                business.getOwnerName(),
                business.getDistrict());
    }
}