package in.wb.nhls.license.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.license.LicenseApplicationRepository;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.license.LicenseRenewalRepository;
import in.wb.nhls.license.LicenseRepository;
import in.wb.nhls.license.dto.LicenseRenewalResponse;
import in.wb.nhls.license.dto.LicenseResponse;
import in.wb.nhls.license.entity.License;
import in.wb.nhls.license.entity.LicenseApplication;
import in.wb.nhls.license.entity.LicenseRenewal;
import in.wb.nhls.license.entity.LicenseStatus;
import in.wb.nhls.user.service.CurrentUser;

@Service
public class LicenseService {

    private static final String LICENSE_NOT_FOUND = "এই license number-এর কোনো license পাওয়া যায়নি";

    private final LicenseRepository licenseRepository;
    private final LicenseApplicationRepository applicationRepository;
    private final LicenseRenewalRepository licenseRenewalRepository;

    public LicenseService(
            LicenseRepository licenseRepository,
            LicenseApplicationRepository applicationRepository, LicenseRenewalRepository licenseRenewalRepository) {
        this.licenseRepository = licenseRepository;
        this.applicationRepository = applicationRepository;
        this.licenseRenewalRepository = licenseRenewalRepository;
    }

    @Auditable(action = "LICENSE_GENERATE", entityType = "LICENSE",
            entityId = "#result?.licenseNumber",
            detail = "'application: ' + #applicationNumber")
    @Transactional
    public License generateLicense(String applicationNumber) {

        LicenseApplication application = applicationRepository
                .findByApplicationNumberForUpdate(applicationNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "এই Application Number-এ কোনো আবেদন পাওয়া যায়নি"));

        if (application.getApplicationStatus() != LicenseApplicationStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "শুধুমাত্র অনুমোদিত application-এর জন্য license তৈরি করা যাবে");
        }

        if (licenseRepository.existsByApplicationId(application.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "এই application-এর জন্য license ইতিমধ্যে তৈরি হয়েছে");
        }

        License license = new License();

        license.setApplication(application);
        license.setLicenseNumber("NHLS-LIC-" + UUID.randomUUID());
        license.setIssuedDate(LocalDate.now());
        license.setValidUntil(LocalDate.now().plusYears(1));
        license.setStatus(LicenseStatus.ACTIVE);

        return licenseRepository.save(license);
    }

    @Transactional(readOnly = true)
    public License getLicenseByNumber(String licenseNumber) {

        return findAccessible(licenseNumber);
    }

    @Transactional(readOnly = true)
    public Page<LicenseResponse> getAllLicenses(Pageable pageable) {

        Page<License> licenses = CurrentUser.isStaff()
                ? licenseRepository.findAll(pageable)
                : licenseRepository.findByApplication_Business_Owner_Username(CurrentUser.username(), pageable);

        return licenses.map(LicenseResponse::fromEntity);
    }

    @Auditable(action = "LICENSE_RENEW", entityType = "LICENSE",
            entityId = "#licenseNumber",
            detail = "'valid until: ' + #result?.validUntil")
    @Transactional
    public License renewLicense(String licenseNumber) {

        License license = findAccessible(licenseNumber);

        if (license.getStatus() == LicenseStatus.INACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "এই license inactive করা হয়েছে, renew করা যাবে না");
        }

        LocalDate previousValidUntil = license.getValidUntil();
        LocalDate newValidUntil = previousValidUntil.plusYears(1);

        LicenseRenewal renewal = new LicenseRenewal();

        renewal.setLicense(license);
        renewal.setPreviousValidUntil(previousValidUntil);
        renewal.setNewValidUntil(newValidUntil);
        renewal.setRenewalDate(LocalDate.now());

        license.setValidUntil(newValidUntil);
        license.setStatus(LicenseStatus.ACTIVE);

        licenseRenewalRepository.save(renewal);

        return licenseRepository.save(license);
    }

    @Auditable(action = "LICENSE_EXPIRE_BATCH", entityType = "LICENSE",
            detail = "'expired count: ' + #result")
    @Transactional
    public int expireLicenses() {

        List<License> licenses = licenseRepository.findAll();

        LocalDate today = LocalDate.now();

        int expiredCount = 0;

        for (License license : licenses) {

            if (license.getStatus() == LicenseStatus.ACTIVE
                    && license.getValidUntil().isBefore(today)) {

                license.setStatus(LicenseStatus.EXPIRED);

                licenseRepository.save(license);

                expiredCount++;
            }
        }

        return expiredCount;
    }

    @Transactional(readOnly = true)
    public List<LicenseRenewalResponse> getRenewalHistory(String licenseNumber) {

        License license = findAccessible(licenseNumber);

        return licenseRenewalRepository
                .findByLicenseIdOrderByRenewalDateDesc(license.getId())
                .stream()
                .map(LicenseRenewalResponse::fromEntity)
                .toList();
    }

    // অন্যের license হলে 404
    private License findAccessible(String licenseNumber) {
        License license = licenseRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, LICENSE_NOT_FOUND));

        if (!CurrentUser.canAccess(license.getApplication().getBusiness().getOwner())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LICENSE_NOT_FOUND);
        }
        return license;
    }
}
