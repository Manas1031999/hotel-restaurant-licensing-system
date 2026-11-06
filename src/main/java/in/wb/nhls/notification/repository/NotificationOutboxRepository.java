package in.wb.nhls.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.wb.nhls.notification.entity.NotificationOutbox;
import in.wb.nhls.notification.entity.NotificationStatus;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    // SKIP LOCKED: একাধিক server চললেও একই row দুজন নেবে না
    @Query(value = """
            SELECT * FROM notification_outbox
            WHERE status = 'PENDING' AND next_attempt_at <= :now
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<NotificationOutbox> lockDueBatch(@Param("now") LocalDateTime now, @Param("limit") int limit);

    Page<NotificationOutbox> findByStatus(NotificationStatus status, Pageable pageable);
}
