package tn.esprit.pi_back.dto.ReturnPayment;

public record ReturnPaymentIntentResponse(
        Long returnPaymentId,
        String paymentIntentId,
        String clientSecret,
        String publishableKey,
        Double amount,
        String currency
) {}
