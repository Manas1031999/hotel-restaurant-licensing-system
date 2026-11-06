package in.wb.nhls.notification.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.wb.nhls.business.dto.ApiResponse;
import in.wb.nhls.business.dto.PaginationResponse;
import in.wb.nhls.notification.dto.NotificationResponse;
import in.wb.nhls.notification.entity.NotificationStatus;
import in.wb.nhls.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> rows = notificationService.list(status, page, size);

        PaginationResponse pagination = new PaginationResponse(
                rows.getNumber(), rows.getSize(), rows.getTotalElements(), rows.getTotalPages());

        return ResponseEntity.ok(new ApiResponse<>(true, "Notification-এর তালিকা", rows.getContent(), pagination));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<NotificationResponse>> retry(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Notification আবার পাঠানোর জন্য রাখা হয়েছে", notificationService.retry(id), null));
    }
}
