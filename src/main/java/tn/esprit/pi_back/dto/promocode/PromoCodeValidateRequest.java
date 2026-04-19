package tn.esprit.pi_back.dto.promocode;

import jakarta.validation.constraints.*;

public record PromoCodeValidateRequest(
        @NotBlank String code,
        @NotNull @PositiveOrZero Double orderAmount
) {}