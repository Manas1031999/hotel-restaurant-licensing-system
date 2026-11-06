package in.wb.nhls.notification.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import in.wb.nhls.notification.template.NotificationTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_outbox", indexes = @Index(name = "idx_outbox_due", columnList = "status, next_attempt_at"))
public class NotificationOutbox {

    private static final Duration BASE_BACKOFF = Duration.ofSeconds(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationTemplate template;

    @Column(nullable = false, length = 150)
    private String recipient;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    protected NotificationOutbox() {
    }

    public NotificationOutbox(NotificationTemplate template, String recipient, String subject, String body) {
        this.template = template;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.nextAttemptAt = this.createdAt;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.attempts++;
        this.lastError = null;
    }

    // ৩০s, ১m, ২m, ৪m... পরে আবার চেষ্টা; maxAttempts পার হলে FAILED
    public void markAttemptFailed(String error, int maxAttempts) {
        this.attempts++;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 1000));
        if (attempts >= maxAttempts) {
            this.status = NotificationStatus.FAILED;
        } else {
            this.nextAttemptAt = LocalDateTime.now().plus(BASE_BACKOFF.multipliedBy(1L << (attempts - 1)));
        }
    }

    public void retry() {
        this.status = NotificationStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public NotificationTemplate getTemplate() {
        return template;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
