package tn.esprit.pi_back.dto.evaluation;

import jakarta.validation.constraints.*;

public record EvaluationRisqueRequestDTO(

        @NotNull
        Double score,

        @NotNull
        @PositiveOrZero
        Double probabiliteDefaut,

        @NotBlank
        String versionModele
) {}