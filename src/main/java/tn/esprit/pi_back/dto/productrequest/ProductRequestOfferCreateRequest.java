package tn.esprit.pi_back.dto.productrequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductRequestOfferCreateRequest(

        @Positive(message = "proposedPrice must be > 0")
        Double proposedPrice,

        @Positive(message = "proposedQuantity must be > 0")
        Integer proposedQuantity,

        @Positive(message = "estimatedDeliveryDays must be > 0")
        Integer estimatedDeliveryDays,

        @Size(max = 2000, message = "message must not exceed 2000 characters")
        String message,

        @NotNull(message = "productId is required")
        Long productId
) {
}