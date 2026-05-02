package tn.esprit.pi_back.dto.demande;

import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.entities.enums.TypeCredit;

import java.time.LocalDateTime;

public record DemandeCreditResponseDTO(

        Long id,
        String reference,
        TypeCredit typeCredit,
        Double montantDemande,
        Integer dureeMois,
        String objetCredit,
        StatutDemande statut,
        LocalDateTime dateCreation,
        Long clientId,
        String clientName,
        Long voucherId,
        String voucherCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}