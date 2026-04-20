package tn.esprit.pi_back.dto.productrequest;

import tn.esprit.pi_back.entities.enums.ProductRequestOfferStatus;

import java.time.LocalDateTime;

public record ProductRequestOfferResponse(
        Long id,

        Long productRequestId,

        Long sellerId,
        String sellerName,

        Long productId,
        String productName,

        Double proposedPrice,
        Integer proposedQuantity,
        Integer estimatedDeliveryDays,
        String message,

        ProductRequestOfferStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}