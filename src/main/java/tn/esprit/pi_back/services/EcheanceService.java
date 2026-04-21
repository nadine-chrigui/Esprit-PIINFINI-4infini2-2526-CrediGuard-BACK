package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.echeance.*;

import java.util.List;

public interface EcheanceService {

    List<EcheanceResponseDTO> getByCredit(Long creditId);

    EcheanceResponseDTO pay(Long echeanceId, EcheancePaymentDTO dto);
}