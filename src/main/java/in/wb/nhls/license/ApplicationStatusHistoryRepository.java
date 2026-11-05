package in.wb.nhls.license;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.license.entity.ApplicationStatusHistory;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {

    // পুরনো থেকে নতুন — timeline হিসেবে দেখানোর জন্য
    List<ApplicationStatusHistory> findByApplicationIdOrderByChangedAtAscIdAsc(Long applicationId);
}
