package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.CompteType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteFinancierResponse {

    private Long idCompte;
    private Double solde;
    private CompteType typeCompte;
    private Long utilisateurId;
}
