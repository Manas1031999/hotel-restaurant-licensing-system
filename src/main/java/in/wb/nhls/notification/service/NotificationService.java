package in.wb.nhls.notification.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.notification.dto.NotificationResponse;
import in.wb.nhls.notification.entity.NotificationOutbox;
import in.wb.nhls.notification.entity.NotificationStatus;
import in.wb.nhls.notification.repository.NotificationOutboxRepository;
import in.wb.nhls.notification.template.NotificationTemplate;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationOutboxRepository outboxRepository;

    public NotificationService(NotificationOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    // MANDATORY: চলমান business transaction-এর ভেতরেই লিখতে হবে, নাহলে error
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(NotificationTemplate template, String recipient, Map<String, ?> params) {
        if (recipient == null || recipient.isBlank()) {
            log.warn("Notification বাদ গেল, recipient নেই: template={}", template);
            return;
        }

        outboxRepository.save(new NotificationOutbox(
                template,
                recipient.trim(),
                template.renderSubject(params),
                template.renderBody(params)));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(NotificationStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Order.desc("id")));

        Page<NotificationOutbox> rows = status == null
                ? outboxRepository.findAll(pageable)
                : outboxRepository.findByStatus(status, pageable);

        return rows.map(NotificationResponse::fromEntity);
    }

    @Transactional
    public NotificationResponse retry(Long id) {
        NotificationOutbox outbox = outboxRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification পাওয়া যায়নি"));

        if (outbox.getStatus() != NotificationStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "শুধু FAILED notification আবার পাঠানো যায়");
        }

        outbox.retry();
        return NotificationResponse.fromEntity(outbox);
    }
}
