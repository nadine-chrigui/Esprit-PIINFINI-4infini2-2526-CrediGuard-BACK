package tn.esprit.pi_back.dto.PurchaseOption;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PurchaseOptionCreateRequest(

        @NotNull @Positive
        Double fixedPrice,

        @NotNull @Min(1)
        Integer maxQuantity,

        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        Double commissionRate,

        @NotNull
        LocalDate expirationDate,

        @NotNull
        Long projectId

) {}
