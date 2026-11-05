package in.wb.nhls.license.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.business.entity.Business;
import in.wb.nhls.business.repository.BusinessRepository;
import in.wb.nhls.license.ApplicationStatusHistoryRepository;
import in.wb.nhls.license.LicenseApplicationRepository;
import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.license.dto.ApplicationStatusHistoryResponse;
import in.wb.nhls.license.dto.LicenseApplicationRequest;
import in.wb.nhls.license.dto.LicenseApplicationResponse;
import in.wb.nhls.license.dto.LicenseApplicationReviewRequest;
import in.wb.nhls.license.entity.ApplicationStatusHistory;
import in.wb.nhls.license.entity.LicenseApplication;
import in.wb.nhls.user.entity.Role;
import in.wb.nhls.user.service.CurrentUser;

@Service
public class LicenseApplicationService {

    // কোন status থেকে কোথায় যাওয়া যাবে
    private static final Map<LicenseApplicationStatus, Set<LicenseApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
            LicenseApplicationStatus.PENDING, Set.of(LicenseApplicationStatus.UNDER_REVIEW),
            LicenseApplicationStatus.UNDER_REVIEW, Set.of(
                    LicenseApplicationStatus.APPROVED,
                    LicenseApplicationStatus.REJECTED));

    // কোন status কে দিতে পারবে: review করে REVIEWER, চূড়ান্ত সিদ্ধান্ত APPROVER
    private static final Map<LicenseApplicationStatus, Role> REQUIRED_ROLE = Map.of(
            LicenseApplicationStatus.UNDER_REVIEW, Role.REVIEWER,
            LicenseApplicationStatus.APPROVED, Role.APPROVER,
            LicenseApplicationStatus.REJECTED, Role.APPROVER);

    private final LicenseApplicationRepository licenseApplicationRepository;
    private final BusinessRepository businessRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;

    public LicenseApplicationService(
            LicenseApplicationRepository licenseApplicationRepository,
            BusinessRepository businessRepository,
            ApplicationStatusHistoryRepository statusHistoryRepository) {

        this.licenseApplicationRepository = licenseApplicationRepository;
        this.businessRepository = businessRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Auditable(action = "APPLICATION_SUBMIT", entityType = "LICENSE_APPLICATION",
            entityId = "#result?.applicationNumber",
            detail = "'businessId: ' + #request.businessId()")
    @Transactional
    public LicenseApplication submitApplication(
            LicenseApplicationRequest request) {

        Business business = businessRepository.findById(request.businessId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ব্যবসাটি খুঁজে পাওয়া যায়নি"));

        if (!business.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "নিষ্ক্রিয় ব্যবসার জন্য আবেদন করা যাবে না");
        }

        LicenseApplication application = new LicenseApplication();

        application.setBusiness(business);
        application.setApplicationStatus(
                LicenseApplicationStatus.PENDING);

        application = licenseApplicationRepository.save(application);

        String applicationNumber = String.format(
                "NHLS-APP-%d-%05d",
                application.getSubmittedAt().getYear(),
                application.getId());

        application.setApplicationNumber(applicationNumber);

        recordStatusChange(
                application,
                null,
                LicenseApplicationStatus.PENDING,
                "আবেদন জমা দেওয়া হয়েছে");

        return licenseApplicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public LicenseApplication getApplicationByNumber(String applicationNumber) {

        return licenseApplicationRepository
                .findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "এই Application Number-এ কোনো আবেদন পাওয়া যায়নি"));
    }

    @Auditable(action = "APPLICATION_REVIEW", entityType = "LICENSE_APPLICATION",
            entityId = "#applicationNumber",
            detail = "'requested status: ' + #request.applicationStatus()")
    @Transactional
    public LicenseApplication reviewApplication(
            String applicationNumber,
            LicenseApplicationReviewRequest request) {

        LicenseApplication application = licenseApplicationRepository
                .findByApplicationNumberForUpdate(applicationNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "এই Application Number-এ কোনো আবেদন পাওয়া যায়নি"));

        LicenseApplicationStatus currentStatus = application.getApplicationStatus();

        LicenseApplicationStatus newStatus = request.applicationStatus();

        Set<LicenseApplicationStatus> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        // APPROVED বা REJECTED হলে আর কোনো পরিবর্তন করা যাবে না
        if (allowedNext.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "এই আবেদনটি ইতিমধ্যে চূড়ান্তভাবে নিষ্পত্তি হয়েছে");
        }

        if (!allowedNext.contains(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    currentStatus.name() + " থেকে " + newStatus.name()
                            + " করা যাবে না। অনুমোদিত: " + allowedNext);
        }

        Role requiredRole = REQUIRED_ROLE.get(newStatus);
        if (!CurrentUser.hasRole(requiredRole)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    newStatus.name() + " করার অনুমতি শুধু " + requiredRole.name() + "-এর আছে");
        }

        application.setApplicationStatus(newStatus);
        application.setRemarks(request.remarks());

        recordStatusChange(application, currentStatus, newStatus, request.remarks());

        // চূড়ান্ত সিদ্ধান্ত হলে review date set হবে
        if (newStatus == LicenseApplicationStatus.APPROVED
                || newStatus == LicenseApplicationStatus.REJECTED) {

            application.setReviewedAt(LocalDateTime.now());
        }

        return licenseApplicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public Page<LicenseApplicationResponse> getApplicationsByStatus(
            LicenseApplicationStatus applicationStatus,
            Pageable pageable) {

        return licenseApplicationRepository
                .findByApplicationStatusOrderBySubmittedAtDesc(
                        applicationStatus,
                        pageable)
                .map(LicenseApplicationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistoryResponse> getStatusHistory(String applicationNumber) {

        LicenseApplication application = getApplicationByNumber(applicationNumber);

        return statusHistoryRepository
                .findByApplicationIdOrderByChangedAtAscIdAsc(application.getId())
                .stream()
                .map(ApplicationStatusHistoryResponse::fromEntity)
                .toList();
    }

    // Status বদলের প্রতিটি ঘটনা একই transaction-এ history-তে লেখা হয়
    private void recordStatusChange(
            LicenseApplication application,
            LicenseApplicationStatus previousStatus,
            LicenseApplicationStatus newStatus,
            String remarks) {

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setChangedBy(CurrentUser.username());

        statusHistoryRepository.save(history);
    }
}
