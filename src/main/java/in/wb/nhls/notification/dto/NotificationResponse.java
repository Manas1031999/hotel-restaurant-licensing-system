package in.wb.nhls.notification.dto;

import java.time.LocalDateTime;

import in.wb.nhls.notification.entity.NotificationOutbox;
import in.wb.nhls.notification.entity.NotificationStatus;
import in.wb.nhls.notification.template.NotificationTemplate;

public record NotificationResponse(
        Long id,
        NotificationTemplate template,
        String recipient,
        String subject,
        NotificationStatus status,
        int attempts,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime nextAttemptAt,
        LocalDateTime sentAt) {

    public static NotificationResponse fromEntity(NotificationOutbox n) {
        return new NotificationResponse(
                n.getId(),
                n.getTemplate(),
                n.getRecipient(),
                n.getSubject(),
                n.getStatus(),
                n.getAttempts(),
                n.getLastError(),
                n.getCreatedAt(),
                n.getNextAttemptAt(),
                n.getSentAt());
    }
}
