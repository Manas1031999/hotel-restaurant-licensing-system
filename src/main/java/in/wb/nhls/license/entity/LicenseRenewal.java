
package in.wb.nhls.license.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "license_renewals")
public class LicenseRenewal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @Column(nullable = false)
    private LocalDate previousValidUntil;

    @Column(nullable = false)
    private LocalDate newValidUntil;

    @Column(nullable = false)
    private LocalDate renewalDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public LocalDate getPreviousValidUntil() {
        return previousValidUntil;
    }

    public void setPreviousValidUntil(LocalDate previousValidUntil) {
        this.previousValidUntil = previousValidUntil;
    }

    public LocalDate getNewValidUntil() {
        return newValidUntil;
    }

    public void setNewValidUntil(LocalDate newValidUntil) {
        this.newValidUntil = newValidUntil;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDate renewalDate) {
        this.renewalDate = renewalDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
