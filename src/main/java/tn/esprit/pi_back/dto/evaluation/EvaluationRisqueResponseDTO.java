package tn.esprit.pi_back.dto.evaluation;

import tn.esprit.pi_back.entities.enums.DecisionSuggeree;
import tn.esprit.pi_back.entities.enums.NiveauRisque;

import java.time.LocalDateTime;

public record EvaluationRisqueResponseDTO(

        Long id,
        Double score,
        NiveauRisque niveauRisque,
        Double probabiliteDefaut,
        String versionModele,
        DecisionSuggeree decisionSuggeree,
        LocalDateTime dateEvaluation,
        Long demandeId,

        Double scoreBase,
        Double scoreConservateur,
        Double probabiliteDefautBase,
        Double probabiliteDefautConservative,
        Double mcStd,
        Double var95,
        Double var99,
        Double cvar95,
        Double ci95Lower,
        Double ci95Upper,
        Boolean highUncertainty,
        String riskClassBase,
        String riskClassConservative,
        String scoreBandBase,
        String scoreBandConservative,
        String decisionBase,
        String decisionConservative
) {}
