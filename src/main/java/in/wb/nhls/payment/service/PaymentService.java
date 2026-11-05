package in.wb.nhls.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.license.LicenseRepository;
import in.wb.nhls.license.entity.License;
import in.wb.nhls.payment.PaymentRepository;
import in.wb.nhls.payment.dto.PaymentRequest;
import in.wb.nhls.payment.dto.PaymentResponse;
import in.wb.nhls.payment.entity.Payment;
import in.wb.nhls.payment.entity.PaymentStatus;

@Service
public class PaymentService {

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
                .orElseThrow(() -> new RuntimeException(
                        "License not found: " + request.licenseNumber()));

        if (license.getStatus() == null
                || license.getStatus().name().equals("INACTIVE")) {

            throw new RuntimeException(
                    "Payment cannot be initiated for an inactive license");
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
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found: " + transactionReference));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException(
                    "Payment is already processed");
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