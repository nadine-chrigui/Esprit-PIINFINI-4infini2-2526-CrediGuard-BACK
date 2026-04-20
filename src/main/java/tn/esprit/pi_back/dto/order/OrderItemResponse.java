package tn.esprit.pi_back.dto.order;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        Double unitPrice,
        Double lineTotal
) {}