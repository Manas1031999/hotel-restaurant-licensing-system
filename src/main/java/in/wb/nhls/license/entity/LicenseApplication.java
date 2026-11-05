package in.wb.nhls.license.entity;

import java.time.LocalDateTime;

import in.wb.nhls.business.entity.Business;
import in.wb.nhls.license.LicenseApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "license_applications")
public class LicenseApplication {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 30)
    private LicenseApplicationStatus applicationStatus;

    @Column(name = "application_number", unique = true, length = 50)
    private String applicationNumber;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    public LicenseApplication() {
    }

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();

        if (this.applicationStatus == null) {
            this.applicationStatus = LicenseApplicationStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public LicenseApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(
            LicenseApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
