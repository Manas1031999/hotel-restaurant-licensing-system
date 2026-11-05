package in.wb.nhls.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.wb.nhls.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByLicenseIdOrderByCreatedAtDesc(Long licenseId);

    Optional<Payment> findByTransactionReference(String transactionReference);
}