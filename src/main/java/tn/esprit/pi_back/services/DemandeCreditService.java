package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.demande.DemandeCreditRequestDTO;
import tn.esprit.pi_back.dto.demande.DemandeCreditResponseDTO;
import tn.esprit.pi_back.entities.enums.StatutDemande;

import java.util.List;

public interface DemandeCreditService {

    DemandeCreditResponseDTO create(String email, DemandeCreditRequestDTO dto);

    DemandeCreditResponseDTO getById(Long id);

    List<DemandeCreditResponseDTO> getAll(Long clientId, StatutDemande statut);

    DemandeCreditResponseDTO update(Long id, DemandeCreditRequestDTO dto);

    void delete(Long id);

    DemandeCreditResponseDTO setStatus(Long id, StatutDemande statut);
}