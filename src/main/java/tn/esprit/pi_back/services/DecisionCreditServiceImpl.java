package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.decision.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.*;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DecisionCreditServiceImpl implements DecisionCreditService {

    private final DecisionCreditRepository decisionRepo;
    private final DemandeCreditRepository demandeRepo;

    @Override
    public DecisionCreditResponseDTO create(Long demandeId, DecisionCreditRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (decisionRepo.existsByDemandeCreditId(demandeId))
            throw new IllegalStateException("Decision already exists for this demande");

        if (demande.getStatut() != StatutDemande.EN_COURS_D_ETUDE)
            throw new IllegalStateException("Decision can only be taken if demande is EN_COURS_D_ETUDE");

        DecisionCredit decision = new DecisionCredit();
        decision.setDecisionFinale(dto.decisionFinale());
        decision.setJustification(dto.justification());
        decision.setConditions(dto.conditions());
        decision.setPrisePar(dto.prisePar());
        decision.setDemandeCredit(demande);

        DecisionCredit saved = decisionRepo.save(decision);

        // 🔥 Synchronisation avec Demande
        if (dto.decisionFinale() == DecisionFinale.ACCEPTE)
            demande.setStatut(StatutDemande.APPROUVEE);

        else if (dto.decisionFinale() == DecisionFinale.REFUSE)
            demande.setStatut(StatutDemande.REJETEE);

        // CONDITIONNEL → pas de changement

        return toDTO(saved);
    }

    @Override
    public DecisionCreditResponseDTO getByDemande(Long demandeId) {

        DecisionCredit decision = decisionRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

        return toDTO(decision);
    }

    private DecisionCreditResponseDTO toDTO(DecisionCredit d) {

        return new DecisionCreditResponseDTO(
                d.getId(),
                d.getDecisionFinale(),
                d.getJustification(),
                d.getConditions(),
                d.getDateDecision(),
                d.getPrisePar(),
                d.getDemandeCredit().getId(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}