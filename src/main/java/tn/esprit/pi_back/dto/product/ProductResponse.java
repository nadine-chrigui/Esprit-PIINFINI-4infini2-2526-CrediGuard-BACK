package tn.esprit.pi_back.dto.product;

import tn.esprit.pi_back.entities.enums.PaymentType;
import tn.esprit.pi_back.entities.enums.PricingStrategy;
import tn.esprit.pi_back.entities.enums.SaleMode;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long sellerId,
        String sellerName,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        Double basePrice,
        Double currentPrice,
        boolean dynamicPricingEnabled,
        PricingStrategy pricingStrategy,
        SaleMode saleType,
        Integer stockQuantity,
        Integer preorderQuota,
        Integer preorderCount,
        PaymentType paymentMode,
        Double depositPercentage,
        boolean expressDeliveryAvailable,
        Double expressDeliveryFee,
        LocalDateTime preorderStartDate,
        LocalDateTime preorderEndDate,
        LocalDateTime expectedReleaseDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String imageUrl
) {}