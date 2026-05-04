package tn.esprit.pi_back.dto.plan;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.NatureActivite;

public record PlanUtilisationRequestDTO(

        @NotBlank
        String descriptionProjet,

        @NotBlank
        String objectifCredit,

        @NotNull
        @Positive
        Double montantInvestissement,

        @PositiveOrZero
        Double revenuMensuelPrevu,

        @PositiveOrZero
        Double profitMensuelPrevu,

        @Positive
        Integer delaiRentabiliteMois,

        @NotNull
        NatureActivite natureActivite
) {}