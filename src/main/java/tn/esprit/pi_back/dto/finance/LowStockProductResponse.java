package tn.esprit.pi_back.dto.finance;

public record LowStockProductResponse(
        Long productId,
        String productName,
        String sellerName,
        String categoryName,
        Integer stockQuantity
) {
}