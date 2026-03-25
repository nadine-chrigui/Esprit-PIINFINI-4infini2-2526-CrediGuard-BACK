package tn.esprit.pi_back.dto.evaluation;

import tn.esprit.pi_back.entities.enums.*;

import java.time.LocalDateTime;

public record EvaluationRisqueResponseDTO(

        Long id,
        Double score,
        NiveauRisque niveauRisque,
        Double probabiliteDefaut,
        String versionModele,
        DecisionSuggeree decisionSuggeree,
        LocalDateTime dateEvaluation,
        Long demandeId
) {}