package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.decision.DecisionCreditRequestDTO;
import tn.esprit.pi_back.dto.decision.DecisionCreditResponseDTO;
import tn.esprit.pi_back.entities.DecisionCredit;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.enums.DecisionFinale;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.events.CreditApprovedEvent;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.mappers.DecisionCreditMapper;
import tn.esprit.pi_back.repositories.DecisionCreditRepository;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class DecisionCreditServiceImpl implements DecisionCreditService {

    private final DecisionCreditRepository decisionRepo;
    private final DemandeCreditRepository demandeRepo;
    private final DecisionCreditMapper decisionCreditMapper;
    private final ApplicationEventPublisher eventPublisher; // Ajouté pour les événements

    @Override
    public DecisionCreditResponseDTO create(Long demandeId, DecisionCreditRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (decisionRepo.existsByDemandeCreditId(demandeId)) {
            throw new IllegalStateException("Decision already exists for this demande");
        }

        if (demande.getStatut() != StatutDemande.EN_COURS_D_ETUDE) {
            throw new IllegalStateException("Decision can only be taken if demande is EN_COURS_D_ETUDE");
        }

        DecisionCredit decision = new DecisionCredit();
        decision.setDecisionFinale(dto.decisionFinale());
        decision.setJustification(dto.justification());
        decision.setConditions(dto.conditions());
        decision.setPrisePar(dto.prisePar());
        decision.setDemandeCredit(demande);

        DecisionCredit saved = decisionRepo.save(decision);

        if (dto.decisionFinale() == DecisionFinale.ACCEPTE) {
            demande.setStatut(StatutDemande.APPROUVEE);
            demandeRepo.save(demande); // On force la sauvegarde
            
            // CRUCIAL: On prévient le système que le crédit est approuvé pour générer le voucher
            System.out.println(">>>> [DECISION SERVICE] PUBLISHING CreditApprovedEvent for " + demande.getReference());
            eventPublisher.publishEvent(new CreditApprovedEvent(this, demande));
            
        } else if (dto.decisionFinale() == DecisionFinale.REFUSE) {
            demande.setStatut(StatutDemande.REJETEE);
            demandeRepo.save(demande);
        }

        return decisionCreditMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionCreditResponseDTO getByDemande(Long demandeId) {

        DecisionCredit decision = decisionRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

        return decisionCreditMapper.toResponse(decision);
    }
}