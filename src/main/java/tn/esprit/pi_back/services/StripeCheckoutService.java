package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.stripe.StripeCheckoutResponse;

public interface StripeCheckoutService {
    StripeCheckoutResponse createCheckoutSession(Long paymentId);
    PaymentResponse confirmCheckoutSession(String sessionId);
}
