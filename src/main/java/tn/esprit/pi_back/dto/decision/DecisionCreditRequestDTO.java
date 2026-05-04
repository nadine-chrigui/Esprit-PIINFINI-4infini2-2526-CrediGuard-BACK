package tn.esprit.pi_back.dto.decision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tn.esprit.pi_back.entities.enums.DecisionFinale;

public record DecisionCreditRequestDTO(

        @NotNull
        DecisionFinale decisionFinale,

        @NotBlank
        String justification,

        String conditions,

        @NotBlank
        String prisePar
) {}