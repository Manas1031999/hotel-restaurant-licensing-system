package in.wb.nhls.business.controller;

import in.wb.nhls.business.dto.ApiResponse;
import in.wb.nhls.business.dto.BusinessRequest;
import in.wb.nhls.business.dto.BusinessResponse;
import in.wb.nhls.business.dto.PaginationResponse;
import in.wb.nhls.business.dto.UpdateBusinessRequest;
import in.wb.nhls.business.entity.Business;
import in.wb.nhls.business.service.BusinessService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerBusiness(
            @Valid @RequestBody BusinessRequest request) {

        Business business = businessService.registerBusiness(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "SUCCESS",
                        "message", "ব্যবসার নিবন্ধন সফল হয়েছে",
                        "businessId", business.getId(),
                        "businessName", business.getBusinessName()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getAllBusinesses(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<BusinessResponse> businessPage = businessService.getAllBusinesses(pageable);

        PaginationResponse pagination = new PaginationResponse(
                businessPage.getNumber(),
                businessPage.getSize(),
                businessPage.getTotalElements(),
                businessPage.getTotalPages());

        ApiResponse<List<BusinessResponse>> response = new ApiResponse<>(
                true,
                "ব্যবসার তালিকা পাওয়া গেছে",
                businessPage.getContent(),
                pagination);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(
            @PathVariable Long id) {

        BusinessResponse business = businessService.getBusinessById(id);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "ব্যবসার তথ্য পাওয়া গেছে",
                business,
                null);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBusinessRequest request) {

        BusinessResponse business = businessService.updateBusiness(id, request);

        ApiResponse<BusinessResponse> response = new ApiResponse<>(
                true,
                "ব্যবসার তথ্য সফলভাবে পরিবর্তন হয়েছে",
                business,
                null);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deactivateBusiness(
            @PathVariable Long id) {

        businessService.deactivateBusiness(id);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "ব্যবসাটি সফলভাবে নিষ্ক্রিয় করা হয়েছে"));
    }

}