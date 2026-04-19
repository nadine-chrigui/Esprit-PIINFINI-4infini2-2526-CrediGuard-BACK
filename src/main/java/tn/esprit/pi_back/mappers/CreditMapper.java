package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.credit.CreditResponseDTO;
import tn.esprit.pi_back.entities.Credit;

@Component
public class CreditMapper {

    public CreditResponseDTO toResponse(Credit credit) {
        return new CreditResponseDTO(
                credit.getId(),
                credit.getMontantAccorde(),
                credit.getMontantTotal(),
                credit.getMontantRestant(),
                credit.getTauxRemboursement(),
                credit.getDateDebut(),
                credit.getDateFin(),
                credit.getStatut(),
                credit.getModeRemboursement(),
                credit.getClient() != null ? credit.getClient().getId() : null,
                credit.getDemandeCredit() != null ? credit.getDemandeCredit().getId() : null,
                credit.getCreatedAt(),
                credit.getUpdatedAt()
        );
    }
}