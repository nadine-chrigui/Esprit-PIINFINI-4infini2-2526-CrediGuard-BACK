package tn.esprit.pi_back.dto.finance;

public record TopProductStatsResponse(
        Long productId,
        String productName,
        long quantitySold,
        double revenue
) {
}