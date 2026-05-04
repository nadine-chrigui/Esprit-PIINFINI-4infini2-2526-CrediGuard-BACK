package tn.esprit.pi_back.dto.InvestmentPayment;

public record InvestmentPaymentIntentResponse(
        Long paymentId,
        String paymentIntentId,
        String clientSecret,
        String publishableKey,
        Double amount,
        String currency,
        Double expectedReturn
) {}
