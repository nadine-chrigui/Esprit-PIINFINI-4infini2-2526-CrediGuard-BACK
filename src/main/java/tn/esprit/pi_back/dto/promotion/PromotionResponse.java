package tn.esprit.pi_back.dto.promotion;

import tn.esprit.pi_back.entities.enums.DiscountType;
import tn.esprit.pi_back.entities.enums.PromotionTargetType;
import tn.esprit.pi_back.entities.enums.PromotionType;

import java.time.LocalDateTime;

public record PromotionResponse(
        Long id,
        String name,
        String description,
        PromotionType promotionType,
        DiscountType discountType,
        PromotionTargetType targetType,
        Double discountValue,
        Double minOrderAmount,
        Double maxDiscountAmount,
        Boolean active,
        Integer priority,
        Boolean autoApply,
        Boolean stackable,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long categoryId,
        String categoryName,
        Long productId,
        String productName,
        Long calendarEventId,
        String calendarEventName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}