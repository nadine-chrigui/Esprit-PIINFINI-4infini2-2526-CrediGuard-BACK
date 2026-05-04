package tn.esprit.pi_back.dto.finance;

import lombok.Data;
import tn.esprit.pi_back.entities.Credit;

@Data
public class CreditDto {
    private Long id;
    private Double montantAccorde;
    private Double montantTotal;
    private Double montantRestant;
    private Double tauxRemboursement;
    private String dateDebut;
    private String dateFin;
    private String statut;
    private String modeRemboursement;
    private Long clientId;

    public static CreditDto from(Credit c) {
        CreditDto dto = new CreditDto();
        dto.id = c.getId();
        dto.montantAccorde = c.getMontantAccorde();
        dto.montantTotal = c.getMontantTotal();
        dto.montantRestant = c.getMontantRestant();
        dto.tauxRemboursement = c.getTauxRemboursement();
        dto.dateDebut = c.getDateDebut() != null ? c.getDateDebut().toString() : null;
        dto.dateFin = c.getDateFin() != null ? c.getDateFin().toString() : null;
        dto.statut = c.getStatut() != null ? c.getStatut().name() : null;
        dto.modeRemboursement = c.getModeRemboursement() != null ? c.getModeRemboursement().name() : null;
        dto.clientId = c.getClient() != null ? c.getClient().getId() : null;
        return dto;
    }
}