package tn.esprit.pi_back.dto.promotion;

public record ProductPriceView(
        Long productId,
        Double originalPrice,
        Double finalPrice,
        Double discountAmount,
        Boolean promotionApplied,
        String promotionName
) {}