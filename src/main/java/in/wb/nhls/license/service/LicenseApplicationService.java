package in.wb.nhls.license.service;

import java.time.LocalDateTime;
import java.util.List;

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
import in.wb.nhls.notification.annotation.Notify;
import in.wb.nhls.notification.template.NotificationTemplate;
import in.wb.nhls.user.service.CurrentUser;
import in.wb.nhls.workflow.service.WorkflowService;

@Service
public class LicenseApplicationService {

    private final LicenseApplicationRepository licenseApplicationRepository;
    private final BusinessRepository businessRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final WorkflowService workflowService;

    public LicenseApplicationService(
            LicenseApplicationRepository licenseApplicationRepository,
            BusinessRepository businessRepository,
            ApplicationStatusHistoryRepository statusHistoryRepository,
            WorkflowService workflowService) {

        this.licenseApplicationRepository = licenseApplicationRepository;
        this.businessRepository = businessRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.workflowService = workflowService;
    }

    @Auditable(action = "APPLICATION_SUBMIT", entityType = "LICENSE_APPLICATION",
            entityId = "#result?.applicationNumber",
            detail = "'businessId: ' + #request.businessId()")
    @Notify(template = NotificationTemplate.APPLICATION_SUBMITTED,
            to = "#result.business.email",
            params = "{appNo: #result.applicationNumber, ownerName: #result.business.ownerName,"
                    + " businessName: #result.business.businessName}")
    @Transactional
    public LicenseApplication submitApplication(
            LicenseApplicationRequest request) {

        Business business = businessRepository.findById(request.businessId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ব্যবসাটি খুঁজে পাওয়া যায়নি"));

        // শুধু নিজের ব্যবসার জন্য আবেদন
        if (!CurrentUser.canAccess(business.getOwner())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ব্যবসাটি খুঁজে পাওয়া যায়নি");
        }

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

        LicenseApplication application = licenseApplicationRepository
                .findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "এই Application Number-এ কোনো আবেদন পাওয়া যায়নি"));

        // অন্যের আবেদন হলে 404
        if (!CurrentUser.canAccess(application.getBusiness().getOwner())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "এই Application Number-এ কোনো আবেদন পাওয়া যায়নি");
        }

        return application;
    }

    // বর্তমান user এই আবেদনে কোন কোন status দিতে পারবে
    @Transactional(readOnly = true)
    public List<LicenseApplicationStatus> getAvailableActions(String applicationNumber) {
        LicenseApplication application = getApplicationByNumber(applicationNumber);
        return workflowService.availableActions(
                application.getBusiness().getBusinessType(), application.getApplicationStatus());
    }

    @Auditable(action = "APPLICATION_REVIEW", entityType = "LICENSE_APPLICATION",
            entityId = "#applicationNumber",
            detail = "'requested status: ' + #request.applicationStatus()")
    @Notify(template = NotificationTemplate.APPLICATION_STATUS_CHANGED,
            to = "#result.business.email",
            params = "{appNo: #result.applicationNumber, status: #result.applicationStatus,"
                    + " remarks: #result.remarks, ownerName: #result.business.ownerName,"
                    + " businessName: #result.business.businessName}")
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

        // নিয়ম আসে DB থেকে (workflow_transition): ভুল ধাপে 400, ভুল role-এ 403
        workflowService.checkTransition(
                application.getBusiness().getBusinessType(), currentStatus, newStatus);

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
