package in.wb.nhls.license.dto;

import java.time.LocalDateTime;

import in.wb.nhls.license.entity.LicenseApplication;

public record LicenseApplicationResponse(
        Long applicationId,
        String applicationNumber,
        String applicationStatus,
        String businessName,
        String businessType,
        String ownerName,
        String mobileNumber,
        String district,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String remarks) {

    public static LicenseApplicationResponse fromEntity(
            LicenseApplication application) {

        return new LicenseApplicationResponse(
                application.getId(),
                application.getApplicationNumber(),
                application.getApplicationStatus().name(),
                application.getBusiness().getBusinessName(),
                application.getBusiness().getBusinessType().name(),
                application.getBusiness().getOwnerName(),
                application.getBusiness().getMobileNumber(),
                application.getBusiness().getDistrict(),
                application.getSubmittedAt(),
                application.getReviewedAt(),
                application.getRemarks());
    }
}