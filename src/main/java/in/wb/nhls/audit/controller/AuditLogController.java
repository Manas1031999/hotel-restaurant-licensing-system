package in.wb.nhls.audit.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.wb.nhls.audit.dto.AuditLogResponse;
import in.wb.nhls.audit.entity.AuditOutcome;
import in.wb.nhls.audit.service.AuditLogService;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) AuditOutcome outcome,
            @RequestParam(required = false) String actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLogResponse> logs = auditLogService.search(
                entityType, entityId, action, outcome, actor, page, size);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "logs", logs.getContent(),
                "currentPage", logs.getNumber(),
                "totalItems", logs.getTotalElements(),
                "totalPages", logs.getTotalPages(),
                "pageSize", logs.getSize()));
    }
}
