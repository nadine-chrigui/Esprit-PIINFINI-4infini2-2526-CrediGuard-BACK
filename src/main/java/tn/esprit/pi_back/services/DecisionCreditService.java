package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.decision.DecisionCreditRequestDTO;
import tn.esprit.pi_back.dto.decision.DecisionCreditResponseDTO;

public interface DecisionCreditService {

    DecisionCreditResponseDTO create(Long demandeId, DecisionCreditRequestDTO dto);

    DecisionCreditResponseDTO getByDemande(Long demandeId);
}