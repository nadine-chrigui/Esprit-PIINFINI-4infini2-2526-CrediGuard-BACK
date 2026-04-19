package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.demande.DemandeCreditResponseDTO;
import tn.esprit.pi_back.entities.DemandeCredit;

@Component
public class DemandeCreditMapper {

    public DemandeCreditResponseDTO toResponse(DemandeCredit demande) {
        Long voucherId = demande.getVoucher() != null ? demande.getVoucher().getId() : null;
        Long clientId = demande.getClient() != null ? demande.getClient().getId() : null;
        String clientName = demande.getClient() != null ? demande.getClient().getFullName() : null;

        return new DemandeCreditResponseDTO(
                demande.getId(),
                demande.getReference(),
                demande.getTypeCredit(),
                demande.getMontantDemande(),
                demande.getDureeMois(),
                demande.getObjetCredit(),
                demande.getStatut(),
                demande.getDateCreation(),
                clientId,
                clientName,
                voucherId,
                demande.getCreatedAt(),
                demande.getUpdatedAt()
        );
    }
}