package tn.esprit.pi_back.dto.demande;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.TypeCredit;

public record DemandeCreditRequestDTO(

        @NotNull(message = "typeCredit is required")
        TypeCredit typeCredit,

        @NotNull(message = "montantDemande is required")
        @Positive(message = "montantDemande must be positive")
        Double montantDemande,

        @NotNull(message = "dureeMois is required")
        @Positive(message = "dureeMois must be positive")
        Integer dureeMois,

        @NotBlank(message = "objetCredit is required")
        String objetCredit
) {}