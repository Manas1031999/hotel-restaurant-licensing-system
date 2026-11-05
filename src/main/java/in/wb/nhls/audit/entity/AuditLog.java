package in.wb.nhls.audit.entity;

import java.time.LocalDateTime;

import in.wb.nhls.audit.event.AuditEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 10)
    private AuditOutcome outcome;

    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "request_uri", length = 255)
    private String requestUri;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AuditLog() {
    }

    public static AuditLog from(AuditEvent event) {
        AuditLog log = new AuditLog();
        log.action = event.action();
        log.entityType = event.entityType();
        log.entityId = truncate(event.entityId(), 100);
        log.outcome = event.outcome();
        log.actor = event.actor();
        log.ipAddress = event.ipAddress();
        log.requestUri = truncate(event.requestUri(), 255);
        log.detail = truncate(event.detail(), 1000);
        log.errorMessage = truncate(event.errorMessage(), 1000);
        log.durationMs = event.durationMs();
        log.createdAt = event.occurredAt();
        return log;
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public String getActor() {
        return actor;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getDetail() {
        return detail;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
