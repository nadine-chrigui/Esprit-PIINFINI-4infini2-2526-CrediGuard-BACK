package tn.esprit.pi_back.dto.finance;

public record PaymentMethodStatsResponse(
        String paymentMethod,
        long count,
        double totalAmount
) {
}