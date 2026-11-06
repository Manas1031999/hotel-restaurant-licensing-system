package in.wb.nhls.workflow.entity;

import java.time.LocalDateTime;

import in.wb.nhls.business.entity.BusinessType;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.user.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// একটা row = একটা নিয়ম; businessType null মানে সব ধরনের ব্যবসার default নিয়ম
@Entity
@Table(name = "workflow_transition",
        indexes = @Index(name = "idx_workflow_lookup", columnList = "business_type, from_status, active"))
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 30)
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, length = 30)
    private LicenseApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private LicenseApplicationStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_role", nullable = false, length = 20)
    private Role requiredRole;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected WorkflowTransition() {
    }

    public WorkflowTransition(BusinessType businessType, LicenseApplicationStatus fromStatus,
            LicenseApplicationStatus toStatus, Role requiredRole) {
        this.businessType = businessType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.requiredRole = requiredRole;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public LicenseApplicationStatus getFromStatus() {
        return fromStatus;
    }

    public LicenseApplicationStatus getToStatus() {
        return toStatus;
    }

    public Role getRequiredRole() {
        return requiredRole;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
