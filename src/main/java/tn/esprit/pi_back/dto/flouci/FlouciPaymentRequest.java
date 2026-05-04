package tn.esprit.pi_back.dto.flouci;

import jakarta.validation.constraints.NotNull;

public record FlouciPaymentRequest(
        @NotNull Long paymentId
) {}
