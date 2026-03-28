package tn.esprit.pi_back.dto.product;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.PaymentType;
import tn.esprit.pi_back.entities.enums.PricingStrategy;
import tn.esprit.pi_back.entities.enums.SaleMode;

import java.time.LocalDateTime;

public record ProductCreateRequest(

        @NotNull(message = "categoryId is required")
        Long categoryId,

        @NotBlank(message = "name is required")
        @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
        String name,

        @Size(max = 2000, message = "description too long")
        String description,

        @NotNull(message = "basePrice is required")
        @Positive(message = "basePrice must be > 0")
        Double basePrice,

        Boolean dynamicPricingEnabled,
        PricingStrategy pricingStrategy,

        @NotNull(message = "saleType is required")
        SaleMode saleType,

        @PositiveOrZero(message = "stockQuantity must be >= 0")
        Integer stockQuantity,

        @Positive(message = "preorderQuota must be > 0")
        Integer preorderQuota,

        PaymentType paymentMode,

        @DecimalMin(value = "0.0", inclusive = false, message = "depositPercentage must be > 0")
        @DecimalMax(value = "1.0", message = "depositPercentage must be <= 1 (ex: 0.2 = 20%)")
        Double depositPercentage,

        Boolean expressDeliveryAvailable,

        @PositiveOrZero(message = "expressDeliveryFee must be >= 0")
        Double expressDeliveryFee,

        LocalDateTime preorderStartDate,
        LocalDateTime preorderEndDate,
        LocalDateTime expectedReleaseDate

) {}