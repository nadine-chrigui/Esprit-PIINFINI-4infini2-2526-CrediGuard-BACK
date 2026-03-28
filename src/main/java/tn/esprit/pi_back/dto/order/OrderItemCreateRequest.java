package tn.esprit.pi_back.dto.order;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemCreateRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {}