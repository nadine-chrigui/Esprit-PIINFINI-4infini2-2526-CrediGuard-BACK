package tn.esprit.pi_back.dto.Payment;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pi_back.entities.enums.PaymentType;

public record PaymentCreateRequest(
        @NotNull Long orderId,
        @NotNull PaymentType paymentType
) {}