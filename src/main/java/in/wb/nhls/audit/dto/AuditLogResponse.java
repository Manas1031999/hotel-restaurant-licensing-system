package in.wb.nhls.audit.dto;

import java.time.LocalDateTime;

import in.wb.nhls.audit.entity.AuditLog;
import in.wb.nhls.audit.entity.AuditOutcome;

public record AuditLogResponse(
        Long auditId,
        String action,
        String entityType,
        String entityId,
        AuditOutcome outcome,
        String actor,
        String ipAddress,
        String requestUri,
        String detail,
        String errorMessage,
        long durationMs,
        LocalDateTime createdAt) {

    public static AuditLogResponse fromEntity(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOutcome(),
                log.getActor(),
                log.getIpAddress(),
                log.getRequestUri(),
                log.getDetail(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getCreatedAt());
    }
}
