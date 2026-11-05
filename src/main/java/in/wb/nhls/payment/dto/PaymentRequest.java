package in.wb.nhls.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(

        @NotBlank(message = "License number is required") String licenseNumber

) {
}