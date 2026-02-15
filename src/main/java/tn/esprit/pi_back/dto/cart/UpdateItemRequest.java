package tn.esprit.pi_back.dto.cart;

import jakarta.validation.constraints.Positive;

public record UpdateItemRequest(
        @Positive Integer quantity
) {}