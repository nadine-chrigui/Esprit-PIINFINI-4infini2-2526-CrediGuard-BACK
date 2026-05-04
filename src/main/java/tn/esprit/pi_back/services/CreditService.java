package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.credit.CreditRequestDTO;
import tn.esprit.pi_back.dto.credit.CreditResponseDTO;
import tn.esprit.pi_back.entities.enums.StatutCredit;

import java.util.List;

public interface CreditService {

    CreditResponseDTO create(Long demandeId, CreditRequestDTO dto);

    CreditResponseDTO getById(Long id);

    List<CreditResponseDTO> getAll(Long clientId, StatutCredit statut);

    CreditResponseDTO update(Long id, CreditRequestDTO dto);

    void delete(Long id);
    CreditResponseDTO getByDemande(Long demandeId);
    CreditResponseDTO changeStatus(Long id, StatutCredit statut);
}