package tn.esprit.pi_back.dto.stripe;

import jakarta.validation.constraints.NotNull;

public record StripeCheckoutRequest(
        @NotNull Long paymentId
) {}
