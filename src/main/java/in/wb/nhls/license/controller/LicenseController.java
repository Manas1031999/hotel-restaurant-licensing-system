package in.wb.nhls.license.controller;

import in.wb.nhls.license.dto.LicenseRenewalResponse;
import in.wb.nhls.license.dto.LicenseResponse;
import in.wb.nhls.license.entity.License;
import in.wb.nhls.license.service.LicenseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping("/generate/{applicationNumber}")
    public ResponseEntity<LicenseResponse> generateLicense(
            @PathVariable String applicationNumber) {

        License license = licenseService.generateLicense(applicationNumber);

        LicenseResponse response = LicenseResponse.fromEntity(license);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{licenseNumber}")
    public ResponseEntity<LicenseResponse> getLicenseByNumber(
            @PathVariable String licenseNumber) {

        License license = licenseService.getLicenseByNumber(licenseNumber);

        LicenseResponse response = LicenseResponse.fromEntity(license);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<LicenseResponse>> getAllLicenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LicenseResponse> licenses = licenseService.getAllLicenses(pageable);

        return ResponseEntity.ok(licenses);
    }

    @PostMapping("/renew/{licenseNumber}")
    public ResponseEntity<LicenseResponse> renewLicense(
            @PathVariable String licenseNumber) {

        License license = licenseService.renewLicense(licenseNumber);

        return ResponseEntity.ok(LicenseResponse.fromEntity(license));
    }

    @PostMapping("/expire")
    public ResponseEntity<Map<String, Object>> expireLicenses() {

        int expiredCount = licenseService.expireLicenses();

        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("message", "Expired licenses processed successfully");
        response.put("expiredCount", expiredCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{licenseNumber}/renewal-history")
    public ResponseEntity<List<LicenseRenewalResponse>> getRenewalHistory(
            @PathVariable String licenseNumber) {

        return ResponseEntity.ok(
                licenseService.getRenewalHistory(licenseNumber));
    }
}