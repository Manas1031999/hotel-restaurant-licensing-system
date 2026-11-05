
package in.wb.nhls.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.wb.nhls.payment.dto.PaymentRequest;
import in.wb.nhls.payment.dto.PaymentResponse;
import in.wb.nhls.payment.service.PaymentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.initiatePayment(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/mock/{transactionReference}")
    public ResponseEntity<PaymentResponse> completeMockPayment(
            @PathVariable String transactionReference,
            @RequestParam boolean success) {

        PaymentResponse response = paymentService.completeMockPayment(
                transactionReference, success);

        return ResponseEntity.ok(response);
    }
}
