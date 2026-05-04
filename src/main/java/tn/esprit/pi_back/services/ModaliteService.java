package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.evaluation.ModaliteRequestDTO;
import tn.esprit.pi_back.dto.evaluation.ModaliteResponseDTO;

public interface ModaliteService {

    ModaliteResponseDTO generate(Long demandeId);

    ModaliteResponseDTO choose(Long demandeId, ModaliteRequestDTO dto);

    ModaliteResponseDTO getByDemande(Long demandeId);
}
