package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.evaluation.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.*;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.*;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationRisqueServiceImpl implements EvaluationRisqueService {

    private final EvaluationRisqueRepository evaluationRepo;
    private final DemandeCreditRepository demandeRepo;

    @Override
    public EvaluationRisqueResponseDTO create(Long demandeId, EvaluationRisqueRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (evaluationRepo.existsByDemandeCreditId(demandeId))
            throw new IllegalStateException("Evaluation already exists for this demande");

        if (demande.getStatut() != StatutDemande.EN_COURS_D_ETUDE)
            throw new IllegalStateException("Evaluation possible only if demande is EN_COURS_D_ETUDE");

        EvaluationRisque evaluation = new EvaluationRisque();

        evaluation.setScore(dto.score());
        evaluation.setProbabiliteDefaut(dto.probabiliteDefaut());
        evaluation.setVersionModele(dto.versionModele());
        evaluation.setDemandeCredit(demande);

        // 🔥 Calcul automatique niveau risque
        if (dto.probabiliteDefaut() < 0.2) {
            evaluation.setNiveauRisque(NiveauRisque.FAIBLE);
            evaluation.setDecisionSuggeree(DecisionSuggeree.ACCEPTER);
        }
        else if (dto.probabiliteDefaut() < 0.5) {
            evaluation.setNiveauRisque(NiveauRisque.MOYEN);
            evaluation.setDecisionSuggeree(DecisionSuggeree.CONDITIONS);
        }
        else {
            evaluation.setNiveauRisque(NiveauRisque.ELEVE);
            evaluation.setDecisionSuggeree(DecisionSuggeree.REFUSER);
        }

        EvaluationRisque saved = evaluationRepo.save(evaluation);

        return toDTO(saved);
    }

    @Override
    public EvaluationRisqueResponseDTO getByDemande(Long demandeId) {

        EvaluationRisque evaluation = evaluationRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        return toDTO(evaluation);
    }

    private EvaluationRisqueResponseDTO toDTO(EvaluationRisque e) {

        return new EvaluationRisqueResponseDTO(
                e.getId(),
                e.getScore(),
                e.getNiveauRisque(),
                e.getProbabiliteDefaut(),
                e.getVersionModele(),
                e.getDecisionSuggeree(),
                e.getDateEvaluation(),
                e.getDemandeCredit().getId()
        );
    }
}