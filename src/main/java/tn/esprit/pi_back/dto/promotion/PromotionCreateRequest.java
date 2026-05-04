package tn.esprit.pi_back.dto.promotion;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.DiscountType;
import tn.esprit.pi_back.entities.enums.PromotionTargetType;
import tn.esprit.pi_back.entities.enums.PromotionType;

import java.time.LocalDateTime;

public record PromotionCreateRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must not exceed 150 characters")
        String name,

        @Size(max = 500, message = "description too long")
        String description,

        @NotNull(message = "promotionType is required")
        PromotionType promotionType,

        @NotNull(message = "discountType is required")
        DiscountType discountType,

        @NotNull(message = "targetType is required")
        PromotionTargetType targetType,

        @NotNull(message = "discountValue is required")
        @Positive(message = "discountValue must be > 0")
        Double discountValue,

        @PositiveOrZero(message = "minOrderAmount must be >= 0")
        Double minOrderAmount,

        @PositiveOrZero(message = "maxDiscountAmount must be >= 0")
        Double maxDiscountAmount,

        Boolean active,

        @PositiveOrZero(message = "priority must be >= 0")
        Integer priority,

        Boolean autoApply,
        Boolean stackable,

        LocalDateTime startDate,
        LocalDateTime endDate,

        Long categoryId,
        Long productId,
        Long calendarEventId
) {}