package tn.esprit.pi_back.dto.cart;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        Double unitPrice,
        Double originalUnitPrice,
        Double finalUnitPrice,
        Double discountAmount,
        Boolean promotionApplied,
        String promotionName,
        Integer quantity,
        Double lineTotal
) {}