package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.decision.DecisionCreditResponseDTO;
import tn.esprit.pi_back.entities.DecisionCredit;

@Component
public class DecisionCreditMapper {

    public DecisionCreditResponseDTO toResponse(DecisionCredit decision) {
        return new DecisionCreditResponseDTO(
                decision.getId(),
                decision.getDecisionFinale(),
                decision.getJustification(),
                decision.getConditions(),
                decision.getDateDecision(),
                decision.getPrisePar(),
                decision.getDemandeCredit() != null ? decision.getDemandeCredit().getId() : null,
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }
}