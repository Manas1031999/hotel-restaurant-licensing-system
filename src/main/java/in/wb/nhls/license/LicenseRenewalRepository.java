
package in.wb.nhls.license;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.license.entity.LicenseRenewal;

public interface LicenseRenewalRepository
        extends JpaRepository<LicenseRenewal, Long> {

    List<LicenseRenewal> findByLicenseIdOrderByRenewalDateDesc(Long licenseId);
}
