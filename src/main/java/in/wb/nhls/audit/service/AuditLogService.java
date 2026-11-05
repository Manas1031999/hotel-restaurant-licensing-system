package in.wb.nhls.audit.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.wb.nhls.audit.dto.AuditLogResponse;
import in.wb.nhls.audit.entity.AuditLog;
import in.wb.nhls.audit.entity.AuditOutcome;
import in.wb.nhls.audit.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            String entityType,
            String entityId,
            String action,
            AuditOutcome outcome,
            String actor,
            int page,
            int size) {

        // যে filter null, সেটা বাদ
        Specification<AuditLog> filter = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (entityType != null) predicates.add(cb.equal(root.get("entityType"), entityType));
            if (entityId != null) predicates.add(cb.equal(root.get("entityId"), entityId));
            if (action != null) predicates.add(cb.equal(root.get("action"), action));
            if (outcome != null) predicates.add(cb.equal(root.get("outcome"), outcome));
            if (actor != null) predicates.add(cb.equal(root.get("actor"), actor));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        return auditLogRepository.findAll(filter, pageable)
                .map(AuditLogResponse::fromEntity);
    }
}
