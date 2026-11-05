package in.wb.nhls.license;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.license.entity.License;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByApplicationId(Long applicationId);

    Optional<License> findByLicenseNumber(String licenseNumber);

    boolean existsByApplicationId(Long applicationId);

    // license -> application -> business -> owner -> username
    Page<License> findByApplication_Business_Owner_Username(String username, Pageable pageable);
}
