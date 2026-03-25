package tn.esprit.pi_back.dto.decision;

import tn.esprit.pi_back.entities.enums.DecisionFinale;

import java.time.LocalDateTime;

public record DecisionCreditResponseDTO(

        Long id,
        DecisionFinale decisionFinale,
        String justification,
        String conditions,
        LocalDateTime dateDecision,
        String prisePar,
        Long demandeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}