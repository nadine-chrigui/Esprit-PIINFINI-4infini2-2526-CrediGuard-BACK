package tn.esprit.pi_back.dto.echeance;

import tn.esprit.pi_back.entities.enums.StatutEcheance;

import java.time.LocalDateTime;

public record EcheanceResponseDTO(
        Long id,
        LocalDateTime dateEcheance,
        Double capitalDu,
        Double interetDu,
        StatutEcheance statut,
        Long creditId
) {}