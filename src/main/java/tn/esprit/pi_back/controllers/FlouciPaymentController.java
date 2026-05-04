package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.flouci.FlouciPaymentRequest;
import tn.esprit.pi_back.dto.flouci.FlouciPaymentResponse;
import tn.esprit.pi_back.services.FlouciPaymentService;

@RestController
@RequestMapping("/flouci")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "flouci.enabled", havingValue = "true")
public class FlouciPaymentController {

    private final FlouciPaymentService flouciPaymentService;

    @PostMapping("/payment")
    public ResponseEntity<FlouciPaymentResponse> generatePayment(
            @Valid @RequestBody FlouciPaymentRequest request
    ) {
        return ResponseEntity.ok(flouciPaymentService.generatePayment(request.paymentId()));
    }

    @PostMapping("/payment/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@RequestParam Long paymentId) {
        return ResponseEntity.ok(flouciPaymentService.verifyPayment(paymentId));
    }
}
