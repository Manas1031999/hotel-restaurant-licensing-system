
package in.wb.nhls.license.entity;

import java.time.LocalDateTime;

import in.wb.nhls.license.LicenseApplicationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "application_status_history", indexes = @Index(name = "idx_status_history_application", columnList = "application_id"))
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LicenseApplication application;

    // প্রথম submit-এর সময় আগের কোনো status থাকে না, তাই null হতে পারে
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private LicenseApplicationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LicenseApplicationStatus newStatus;

    @Column(length = 1000)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LicenseApplication getApplication() {
        return application;
    }

    public void setApplication(LicenseApplication application) {
        this.application = application;
    }

    public LicenseApplicationStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(LicenseApplicationStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public LicenseApplicationStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(LicenseApplicationStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
