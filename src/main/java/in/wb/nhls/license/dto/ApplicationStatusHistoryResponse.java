package in.wb.nhls.license.dto;

import java.time.LocalDateTime;

import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.license.entity.ApplicationStatusHistory;

public record ApplicationStatusHistoryResponse(
        Long historyId,
        LicenseApplicationStatus previousStatus,
        LicenseApplicationStatus newStatus,
        String remarks,
        String changedBy,
        LocalDateTime changedAt) {

    public static ApplicationStatusHistoryResponse fromEntity(
            ApplicationStatusHistory history) {

        return new ApplicationStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getRemarks(),
                history.getChangedBy(),
                history.getChangedAt());
    }
}
