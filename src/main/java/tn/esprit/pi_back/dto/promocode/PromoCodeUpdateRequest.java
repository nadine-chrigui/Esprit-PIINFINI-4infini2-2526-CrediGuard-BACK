package tn.esprit.pi_back.dto.promocode;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tn.esprit.pi_back.entities.enums.DiscountType;

import java.time.LocalDateTime;

public record PromoCodeUpdateRequest(
        @Size(min = 3, max = 30) String code,
        DiscountType discountType,
        @Positive Double discountValue,

        Boolean active,
        @Positive Integer maxUses,

        @PositiveOrZero Double minOrderAmount,
        @PositiveOrZero Double maxDiscountAmount,

        LocalDateTime startAt,
        LocalDateTime endAt
) {}