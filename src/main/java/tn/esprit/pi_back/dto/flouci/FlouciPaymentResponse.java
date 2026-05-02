package tn.esprit.pi_back.dto.flouci;

public record FlouciPaymentResponse(
        Long paymentId,
        String flouciPaymentId,
        String paymentLink
) {}
