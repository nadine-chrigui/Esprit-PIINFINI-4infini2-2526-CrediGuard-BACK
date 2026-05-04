package tn.esprit.pi_back.dto.ReturnPayment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReturnPaymentIntentRequest(
        @NotNull
        @Positive
        Long returnPaymentId
) {}
