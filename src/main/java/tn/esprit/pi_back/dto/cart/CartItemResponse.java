package tn.esprit.pi_back.dto.cart;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        Double unitPrice,
        Double originalUnitPrice,
        Double finalUnitPrice,
        Double discountAmount,
        Boolean promotionApplied,
        String promotionName,
        Integer quantity,
        Double lineTotal,
        String source,
        Long sourceOfferId,
        Double negotiatedUnitPrice
) {}
