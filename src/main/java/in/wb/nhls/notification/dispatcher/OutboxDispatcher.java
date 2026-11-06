package in.wb.nhls.notification.dispatcher;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.wb.nhls.notification.entity.NotificationOutbox;
import in.wb.nhls.notification.repository.NotificationOutboxRepository;

// নিয়মিত outbox থেকে PENDING row তুলে email পাঠায়
@Component
@ConditionalOnProperty(name = "nhls.notification.dispatcher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final NotificationOutboxRepository outboxRepository;
    private final JavaMailSender mailSender;
    private final String from;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxDispatcher(NotificationOutboxRepository outboxRepository,
            JavaMailSender mailSender,
            @Value("${nhls.notification.from}") String from,
            @Value("${nhls.notification.batch-size:20}") int batchSize,
            @Value("${nhls.notification.max-attempts:5}") int maxAttempts) {
        this.outboxRepository = outboxRepository;
        this.mailSender = mailSender;
        this.from = from;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${nhls.notification.poll-interval-ms:10000}")
    @Transactional
    public void dispatch() {
        List<NotificationOutbox> batch = outboxRepository.lockDueBatch(LocalDateTime.now(), batchSize);

        for (NotificationOutbox notification : batch) {
            try {
                send(notification);
                notification.markSent();
            } catch (Exception ex) {
                notification.markAttemptFailed(ex.getMessage(), maxAttempts);
                log.warn("Notification {} পাঠানো যায়নি (চেষ্টা {}): {}",
                        notification.getId(), notification.getAttempts(), ex.getMessage());
            }
        }
    }

    private void send(NotificationOutbox notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(notification.getRecipient());
        message.setSubject(notification.getSubject());
        message.setText(notification.getBody());
        mailSender.send(message);
    }
}
