package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.evaluation.EvaluationPredictionRequestDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationRisqueRequestDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationRisqueResponseDTO;

public interface EvaluationRisqueService {

    EvaluationRisqueResponseDTO create(Long demandeId, EvaluationRisqueRequestDTO dto);

    EvaluationRisqueResponseDTO predictWithModel(Long demandeId, EvaluationPredictionRequestDTO dto);

    EvaluationRisqueResponseDTO getByDemande(Long demandeId);
}
