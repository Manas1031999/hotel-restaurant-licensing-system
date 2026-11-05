package in.wb.nhls.license;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.wb.nhls.license.entity.LicenseApplication;
import jakarta.persistence.LockModeType;

public interface LicenseApplicationRepository extends JpaRepository<LicenseApplication, Long> {
    Optional<LicenseApplication> findByApplicationNumber(String applicationNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT la
            FROM LicenseApplication la
            WHERE la.applicationNumber = :applicationNumber
            """)
    Optional<LicenseApplication> findByApplicationNumberForUpdate(
            @Param("applicationNumber") String applicationNumber);

    Page<LicenseApplication> findByApplicationStatus(
            LicenseApplicationStatus applicationStatus,
            Pageable pageable);

    Page<LicenseApplication> findByApplicationStatusOrderBySubmittedAtDesc(
            LicenseApplicationStatus applicationStatus,
            Pageable pageable);

}
