package tn.esprit.pi_back.dto.promocode;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.DiscountType;

import java.time.LocalDateTime;

public record PromoCodeCreateRequest(
        @NotBlank @Size(min = 3, max = 30) String code,
        @NotNull DiscountType discountType,
        @NotNull @Positive Double discountValue,

        Boolean active,
        @NotNull @Positive Integer maxUses,

        @PositiveOrZero Double minOrderAmount,
        @PositiveOrZero Double maxDiscountAmount,

        LocalDateTime startAt,
        LocalDateTime endAt
) {}