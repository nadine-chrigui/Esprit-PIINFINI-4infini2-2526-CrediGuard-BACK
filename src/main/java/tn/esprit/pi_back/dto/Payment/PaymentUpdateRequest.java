package tn.esprit.pi_back.dto.Payment;

import tn.esprit.pi_back.entities.enums.PaymentStatus;

public record PaymentUpdateRequest(
        PaymentStatus paymentStatus,
        String transactionRef
) {}