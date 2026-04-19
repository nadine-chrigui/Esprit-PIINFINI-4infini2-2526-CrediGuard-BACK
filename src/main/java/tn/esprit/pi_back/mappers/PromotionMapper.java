package tn.esprit.pi_back.mappers;

import tn.esprit.pi_back.dto.promotion.PromotionResponse;
import tn.esprit.pi_back.entities.Promotion;

public final class PromotionMapper {

    private PromotionMapper() {}

    public static PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getPromotionType(),
                promotion.getDiscountType(),
                promotion.getTargetType(),
                promotion.getDiscountValue(),
                promotion.getMinOrderAmount(),
                promotion.getMaxDiscountAmount(),
                promotion.getActive(),
                promotion.getPriority(),
                promotion.getAutoApply(),
                promotion.getStackable(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getCategory() != null ? promotion.getCategory().getId() : null,
                promotion.getCategory() != null ? promotion.getCategory().getName() : null,
                promotion.getProduct() != null ? promotion.getProduct().getId() : null,
                promotion.getProduct() != null ? promotion.getProduct().getName() : null,
                promotion.getCalendarEvent() != null ? promotion.getCalendarEvent().getId() : null,
                promotion.getCalendarEvent() != null ? promotion.getCalendarEvent().getName() : null,
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }
}