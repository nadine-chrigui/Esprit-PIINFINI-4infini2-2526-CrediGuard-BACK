package tn.esprit.pi_back.dto.stripe;

public record StripeCheckoutResponse(
        String sessionId,
        String url
) {}
