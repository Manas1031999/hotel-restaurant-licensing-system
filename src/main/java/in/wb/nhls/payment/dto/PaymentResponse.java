package in.wb.nhls.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import in.wb.nhls.payment.entity.Payment;
import in.wb.nhls.payment.entity.PaymentStatus;

public record PaymentResponse(
        Long paymentId,
        String licenseNumber,
        BigDecimal amount,
        PaymentStatus status,
        String transactionReference,
        LocalDateTime paymentDate,
        LocalDateTime createdAt) {

    public static PaymentResponse fromEntity(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getLicense().getLicenseNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getPaymentDate(),
                payment.getCreatedAt());
    }
}