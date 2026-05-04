package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.services.InvestmentPaymentService;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StripeWebhookController {

    private final InvestmentPaymentService investmentPaymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String stripeSignature) {
        investmentPaymentService.handleWebhook(payload, stripeSignature);
        return ResponseEntity.ok().build();
    }
}
