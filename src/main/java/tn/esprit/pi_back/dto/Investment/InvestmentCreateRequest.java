package tn.esprit.pi_back.dto.Investment;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record InvestmentCreateRequest(

        @NotNull @Positive
        Double amount,

        @NotNull
        LocalDate investmentDate,

        @NotNull @PositiveOrZero
        Double expectedReturn,

        @NotNull
        Long investorId,

        @NotNull
        Long projectId

) {}
