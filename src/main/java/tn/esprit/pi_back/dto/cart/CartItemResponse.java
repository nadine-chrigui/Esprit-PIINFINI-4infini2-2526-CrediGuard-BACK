package tn.esprit.pi_back.dto.cart;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Double unitPrice,
        Integer quantity,
        Double lineTotal
) {}