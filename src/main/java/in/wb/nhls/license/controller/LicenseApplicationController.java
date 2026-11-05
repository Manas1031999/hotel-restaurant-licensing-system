package in.wb.nhls.license.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.wb.nhls.license.LicenseApplicationStatus;
import in.wb.nhls.license.dto.ApplicationStatusHistoryResponse;
import in.wb.nhls.license.dto.LicenseApplicationRequest;
import in.wb.nhls.license.dto.LicenseApplicationResponse;
import in.wb.nhls.license.dto.LicenseApplicationReviewRequest;
import in.wb.nhls.license.entity.LicenseApplication;
import in.wb.nhls.license.service.LicenseApplicationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/license-applications")
@Validated
public class LicenseApplicationController {
    private final LicenseApplicationService licenseApplicationService;

    public LicenseApplicationController(
            LicenseApplicationService licenseApplicationService) {
        this.licenseApplicationService = licenseApplicationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitApplication(
            @Valid @RequestBody LicenseApplicationRequest request) {

        LicenseApplication application = licenseApplicationService.submitApplication(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "SUCCESS",
                "message", "লাইসেন্সের আবেদন সফলভাবে জমা হয়েছে",
                "applicationId", application.getId(),
                "applicationNumber", application.getApplicationNumber(),
                "applicationStatus",
                application.getApplicationStatus().name()));
    }

    @GetMapping("/{applicationNumber}")
    public ResponseEntity<LicenseApplicationResponse> getApplicationByNumber(
            @PathVariable String applicationNumber) {

        LicenseApplication application = licenseApplicationService.getApplicationByNumber(applicationNumber);

        LicenseApplicationResponse response = LicenseApplicationResponse.fromEntity(application);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationNumber}/status-history")
    public ResponseEntity<List<ApplicationStatusHistoryResponse>> getStatusHistory(
            @PathVariable String applicationNumber) {

        return ResponseEntity.ok(
                licenseApplicationService.getStatusHistory(applicationNumber));
    }

    @PutMapping("/{applicationNumber}/review")
    public ResponseEntity<Map<String, Object>> reviewApplication(
            @PathVariable String applicationNumber,
            @Valid @RequestBody LicenseApplicationReviewRequest request) {

        LicenseApplication application = licenseApplicationService.reviewApplication(
                applicationNumber,
                request);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "আবেদনটি সফলভাবে review করা হয়েছে",
                "applicationId", application.getId(),
                "applicationNumber", application.getApplicationNumber(),
                "applicationStatus", application.getApplicationStatus().name(),
                "remarks", application.getRemarks()));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApplicationsByStatus(
            @RequestParam(defaultValue = "PENDING") LicenseApplicationStatus applicationStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LicenseApplicationResponse> applications = licenseApplicationService.getApplicationsByStatus(
                applicationStatus,
                pageable);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "applicationStatus", applicationStatus.name(),
                "applications", applications.getContent(),
                "currentPage", applications.getNumber(),
                "totalItems", applications.getTotalElements(),
                "totalPages", applications.getTotalPages(),
                "pageSize", applications.getSize()));
    }
}
