package tn.esprit.pi_back.dto.OptionSubscription;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record OptionSubscriptionCreateRequest(

        @NotNull @Min(1)
        Integer reservedQuantity,

        @NotNull
        LocalDate subscriptionDate,

        @NotNull @PositiveOrZero
        Double amountPaid,

        @NotNull
        Long userId,

        @NotNull
        Long purchaseOptionId

) {}
