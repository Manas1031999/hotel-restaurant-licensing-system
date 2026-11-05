package in.wb.nhls.audit.event;

import java.time.LocalDateTime;

import in.wb.nhls.audit.entity.AuditOutcome;

// Actor/IP request thread-এই ধরা হয়, async listener-এ এগুলো পাওয়া যায় না
public record AuditEvent(
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
        LocalDateTime occurredAt) {

    public boolean isSuccess() {
        return outcome == AuditOutcome.SUCCESS;
    }
}
