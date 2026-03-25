package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.evaluation.*;

public interface EvaluationRisqueService {

    EvaluationRisqueResponseDTO create(Long demandeId, EvaluationRisqueRequestDTO dto);

    EvaluationRisqueResponseDTO getByDemande(Long demandeId);
}