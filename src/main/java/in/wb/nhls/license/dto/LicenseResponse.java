
package in.wb.nhls.license.dto;

import java.time.LocalDate;

import in.wb.nhls.license.entity.License;
import in.wb.nhls.license.entity.LicenseStatus;

public record LicenseResponse(
        Long licenseId,
        String licenseNumber,
        String applicationNumber,
        String businessName,
        String businessType,
        String ownerName,
        LocalDate issuedDate,
        LocalDate validUntil,
        LicenseStatus status) {

    public static LicenseResponse fromEntity(License license) {

        return new LicenseResponse(
                license.getId(),
                license.getLicenseNumber(),
                license.getApplication().getApplicationNumber(),
                license.getApplication().getBusiness().getBusinessName(),
                license.getApplication().getBusiness().getBusinessType().name(),
                license.getApplication().getBusiness().getOwnerName(),
                license.getIssuedDate(),
                license.getValidUntil(),
                license.getStatus());
    }
}
