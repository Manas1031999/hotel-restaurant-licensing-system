package in.wb.nhls.business.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.business.entity.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    List<Business> findByActiveTrue();

    Page<Business> findByActiveTrue(Pageable pageable);

    Page<Business> findByOwnerUsernameAndActiveTrue(String username, Pageable pageable);

}
