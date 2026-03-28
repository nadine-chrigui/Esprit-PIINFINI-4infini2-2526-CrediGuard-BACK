package tn.esprit.pi_back.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemRequest(
        @NotNull Long productId,
        @Positive Integer quantity
) {}