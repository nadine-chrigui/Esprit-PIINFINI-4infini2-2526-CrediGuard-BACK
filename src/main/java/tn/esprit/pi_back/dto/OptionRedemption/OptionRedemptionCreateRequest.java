package tn.esprit.pi_back.dto.OptionRedemption;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record OptionRedemptionCreateRequest(

        @NotNull @Min(1)
        Integer redeemedQuantity,

        @NotNull
        LocalDate redemptionDate,

        @NotNull @Positive
        Double finalPrice,

        @NotNull @PositiveOrZero
        Double commissionAmount,

        @NotNull
        Long subscriptionId,

        @NotNull
        Long orderId

) {}
