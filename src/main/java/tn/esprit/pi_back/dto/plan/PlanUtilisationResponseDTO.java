package tn.esprit.pi_back.dto.plan;

import tn.esprit.pi_back.entities.enums.NatureActivite;

public record PlanUtilisationResponseDTO(

        Long id,
        String descriptionProjet,
        String objectifCredit,
        Double montantInvestissement,
        Double revenuMensuelPrevu,
        Double profitMensuelPrevu,
        Integer delaiRentabiliteMois,
        NatureActivite natureActivite,
        Long demandeId
) {}