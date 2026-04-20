package tn.esprit.pi_back.dto.productrequest;

import tn.esprit.pi_back.entities.enums.ProductRequestStatus;

import java.time.LocalDateTime;

public record ProductRequestResponse(
        Long id,
        String title,
        String description,
        Integer requestedQuantity,
        Double maxBudget,
        LocalDateTime desiredDate,
        String imageUrl,

        ProductRequestStatus status,

        Long clientId,
        String clientName,

        Long categoryId,
        String categoryName,

        Long targetSellerId,
        String targetSellerName,

        int offersCount,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}