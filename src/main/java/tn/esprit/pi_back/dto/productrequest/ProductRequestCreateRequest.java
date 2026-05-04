package tn.esprit.pi_back.dto.productrequest;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProductRequestCreateRequest(

        @NotBlank(message = "title is required")
        @Size(max = 150, message = "title must not exceed 150 characters")
        String title,

        @Size(max = 3000, message = "description must not exceed 3000 characters")
        String description,

        Long categoryId,

        @Positive(message = "requestedQuantity must be > 0")
        Integer requestedQuantity,

        @PositiveOrZero(message = "maxBudget must be >= 0")
        Double maxBudget,

        @FutureOrPresent(message = "desiredDate must be now or in the future")
        LocalDateTime desiredDate,

        Long targetSellerId,

        String imageUrl
) {
}