package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.stripe.StripeCheckoutRequest;
import tn.esprit.pi_back.dto.stripe.StripeCheckoutResponse;
import tn.esprit.pi_back.services.StripeCheckoutService;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StripeCheckoutController {

    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping("/checkout-session")
    public ResponseEntity<StripeCheckoutResponse> createCheckoutSession(
            @Valid @RequestBody StripeCheckoutRequest request
    ) {
        return ResponseEntity.ok(stripeCheckoutService.createCheckoutSession(request.paymentId()));
    }

    @PostMapping("/checkout-session/confirm")
    public ResponseEntity<PaymentResponse> confirmCheckoutSession(@RequestParam String sessionId) {
        return ResponseEntity.ok(stripeCheckoutService.confirmCheckoutSession(sessionId));
    }
}
