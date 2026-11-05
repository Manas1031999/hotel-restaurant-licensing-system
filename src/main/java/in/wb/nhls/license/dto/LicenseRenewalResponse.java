
package in.wb.nhls.license.dto;

import java.time.LocalDate;

import in.wb.nhls.license.entity.LicenseRenewal;

public record LicenseRenewalResponse(
        Long renewalId,
        String licenseNumber,
        LocalDate previousValidUntil,
        LocalDate newValidUntil,
        LocalDate renewalDate) {

    public static LicenseRenewalResponse fromEntity(
            LicenseRenewal renewal) {

        return new LicenseRenewalResponse(
                renewal.getId(),
                renewal.getLicense().getLicenseNumber(),
                renewal.getPreviousValidUntil(),
                renewal.getNewValidUntil(),
                renewal.getRenewalDate());
    }
}
