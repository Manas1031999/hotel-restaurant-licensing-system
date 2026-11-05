package in.wb.nhls.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.license.LicenseRepository;
import in.wb.nhls.license.entity.License;
import in.wb.nhls.license.entity.LicenseStatus;
import in.wb.nhls.payment.PaymentRepository;
import in.wb.nhls.payment.dto.PaymentRequest;
import in.wb.nhls.payment.dto.PaymentResponse;
import in.wb.nhls.payment.entity.Payment;
import in.wb.nhls.payment.entity.PaymentStatus;
import in.wb.nhls.user.service.CurrentUser;

@Service
public class PaymentService {

    private static final String LICENSE_NOT_FOUND = "এই license number-এর কোনো license পাওয়া যায়নি";
    private static final String PAYMENT_NOT_FOUND = "এই payment খুঁজে পাওয়া যায়নি";

    private final PaymentRepository paymentRepository;
    private final LicenseRepository licenseRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            LicenseRepository licenseRepository) {

        this.paymentRepository = paymentRepository;
        this.licenseRepository = licenseRepository;
    }

    @Value("${nhls.license.renewal-fee}")
    private BigDecimal renewalFee;

    @Auditable(action = "PAYMENT_INITIATE", entityType = "PAYMENT",
            entityId = "#result?.transactionReference()",
            detail = "'license: ' + #request.licenseNumber() + ', amount: ' + #result?.amount()")
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {

        License license = licenseRepository
                .findByLicenseNumber(request.licenseNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, LICENSE_NOT_FOUND));

        if (!CurrentUser.canAccess(license.getApplication().getBusiness().getOwner())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LICENSE_NOT_FOUND);
        }

        if (license.getStatus() == null || license.getStatus() == LicenseStatus.INACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inactive license-এর জন্য payment করা যাবে না");
        }

        Payment payment = new Payment();

        payment.setLicense(license);
        payment.setAmount(renewalFee);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionReference(
                "NHLS-PAY-" + UUID.randomUUID());

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Auditable(action = "PAYMENT_COMPLETE", entityType = "PAYMENT",
            entityId = "#transactionReference",
            detail = "'success: ' + #success")
    @Transactional
    public PaymentResponse completeMockPayment(
            String transactionReference,
            boolean success) {

        Payment payment = paymentRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PAYMENT_NOT_FOUND));

        if (!CurrentUser.canAccess(payment.getLicense().getApplication().getBusiness().getOwner())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PAYMENT_NOT_FOUND);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "এই payment ইতিমধ্যে সম্পন্ন হয়েছে");
        }

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.fromEntity(savedPayment);
    }
}
