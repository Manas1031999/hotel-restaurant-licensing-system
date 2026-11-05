package in.wb.nhls.audit.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.wb.nhls.audit.entity.AuditLog;
import in.wb.nhls.audit.event.AuditEvent;
import in.wb.nhls.audit.repository.AuditLogRepository;

// SUCCESS শুধু commit-এর পরে, FAILURE rollback হলেও লেখা হয়
@Component
public class AuditLogListener {

    private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true, condition = "#event.success")
    public void onSuccess(AuditEvent event) {
        save(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION,
            fallbackExecution = true, condition = "!#event.success")
    public void onFailure(AuditEvent event) {
        save(event);
    }

    private void save(AuditEvent event) {
        try {
            auditLogRepository.save(AuditLog.from(event));
        } catch (Exception ex) {
            log.error("Audit log লেখা যায়নি: {}", event, ex);
        }
    }
}
