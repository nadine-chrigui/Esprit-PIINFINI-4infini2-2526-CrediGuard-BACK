package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.plan.*;

import java.util.List;

public interface PlanUtilisationService {

    PlanUtilisationResponseDTO create(Long demandeId, PlanUtilisationRequestDTO dto);

    PlanUtilisationResponseDTO getByDemande(Long demandeId);

    PlanUtilisationResponseDTO update(Long demandeId, PlanUtilisationRequestDTO dto);
    List<PlanUtilisationResponseDTO> getMine(Long clientId);

}