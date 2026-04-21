package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.echeance.EcheanceResponseDTO;
import tn.esprit.pi_back.entities.Echeance;

@Component
public class EcheanceMapper {

    public EcheanceResponseDTO toResponse(Echeance echeance) {
        return new EcheanceResponseDTO(
                echeance.getId(),
                echeance.getDateEcheance(),
                echeance.getCapitalDu(),
                echeance.getInteretDu(),
                echeance.getStatut(),
                echeance.getCredit() != null ? echeance.getCredit().getId() : null
        );
    }
}