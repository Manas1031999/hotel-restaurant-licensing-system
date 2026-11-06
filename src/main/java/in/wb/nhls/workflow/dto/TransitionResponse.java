package in.wb.nhls.workflow.dto;

import in.wb.nhls.business.entity.BusinessType;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.user.entity.Role;
import in.wb.nhls.workflow.entity.WorkflowTransition;

public record TransitionResponse(
        Long id,
        BusinessType businessType,
        LicenseApplicationStatus fromStatus,
        LicenseApplicationStatus toStatus,
        Role requiredRole,
        boolean active) {

    public static TransitionResponse fromEntity(WorkflowTransition t) {
        return new TransitionResponse(
                t.getId(), t.getBusinessType(), t.getFromStatus(), t.getToStatus(), t.getRequiredRole(), t.isActive());
    }
}
