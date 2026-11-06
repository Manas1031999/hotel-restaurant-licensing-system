package in.wb.nhls.workflow.dto;

import in.wb.nhls.business.entity.BusinessType;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.user.entity.Role;
import jakarta.validation.constraints.NotNull;

// businessType না দিলে সব ধরনের ব্যবসার default নিয়ম
public record TransitionRequest(
        BusinessType businessType,

        @NotNull(message = "fromStatus দিতে হবে")
        LicenseApplicationStatus fromStatus,

        @NotNull(message = "toStatus দিতে হবে")
        LicenseApplicationStatus toStatus,

        @NotNull(message = "requiredRole দিতে হবে")
        Role requiredRole) {
}
