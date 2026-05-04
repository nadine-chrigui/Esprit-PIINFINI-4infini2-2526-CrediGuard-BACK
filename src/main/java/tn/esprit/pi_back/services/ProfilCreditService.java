package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.profil.ProfilCreditRequestDTO;
import tn.esprit.pi_back.dto.profil.ProfilCreditResponseDTO;

import java.util.List;

public interface ProfilCreditService {

    ProfilCreditResponseDTO createMyProfile(String email, ProfilCreditRequestDTO dto);

    ProfilCreditResponseDTO updateMyProfile(String email, ProfilCreditRequestDTO dto);

    ProfilCreditResponseDTO getMyProfile(String email);

    ProfilCreditResponseDTO getById(Long id);

    ProfilCreditResponseDTO getByClientId(Long clientId);

    List<ProfilCreditResponseDTO> getAll(Long clientId);

    void delete(Long id);
}
