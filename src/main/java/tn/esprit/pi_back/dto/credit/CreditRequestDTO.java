package tn.esprit.pi_back.dto.credit;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.ModeRemboursement;

import java.time.LocalDateTime;

public record CreditRequestDTO(

        @NotNull
        @Positive
        Double montantAccorde,

        @NotNull
        @PositiveOrZero
        Double tauxRemboursement,

        @NotNull
        ModeRemboursement modeRemboursement,

        @NotNull
        LocalDateTime dateFin
) {}