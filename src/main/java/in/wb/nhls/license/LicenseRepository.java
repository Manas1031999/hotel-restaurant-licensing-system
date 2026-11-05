package in.wb.nhls.license;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.license.entity.License;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByApplicationId(Long applicationId);

    Optional<License> findByLicenseNumber(String licenseNumber);

    boolean existsByApplicationId(Long applicationId);
}
