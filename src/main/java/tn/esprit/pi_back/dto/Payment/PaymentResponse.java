package tn.esprit.pi_back.dto.Payment;

import tn.esprit.pi_back.entities.enums.PaymentStatus;
import tn.esprit.pi_back.entities.enums.PaymentType;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Double amount,
        PaymentType paymentType,
        PaymentStatus paymentStatus,
        String transactionRef,
        String stripeSessionId,
        String stripePaymentIntentId,
        String flouciPaymentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
