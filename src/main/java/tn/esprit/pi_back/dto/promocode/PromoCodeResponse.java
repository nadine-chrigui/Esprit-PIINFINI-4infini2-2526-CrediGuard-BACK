package tn.esprit.pi_back.dto.promocode;
import tn.esprit.pi_back.entities.enums.DiscountType;

import java.time.LocalDateTime;

public record PromoCodeResponse(
        Long id,
        String code,
        DiscountType discountType,
        Double discountValue,
        Boolean active,
        Integer maxUses,
        Integer usedCount,
        Double minOrderAmount,
        Double maxDiscountAmount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}