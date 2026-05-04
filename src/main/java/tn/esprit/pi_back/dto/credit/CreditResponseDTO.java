package tn.esprit.pi_back.dto.credit;

import tn.esprit.pi_back.entities.enums.ModeRemboursement;
import tn.esprit.pi_back.entities.enums.StatutCredit;

import java.time.LocalDateTime;

public record CreditResponseDTO(

        Long id,
        Double montantAccorde,
        Double montantTotal,
        Double montantRestant,
        Double tauxRemboursement,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        StatutCredit statut,
        ModeRemboursement modeRemboursement,
        Long clientId,
        Long demandeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}