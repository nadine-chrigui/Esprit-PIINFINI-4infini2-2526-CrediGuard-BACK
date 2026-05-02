package tn.esprit.pi_back.dto;

import lombok.Data;

@Data
public class CompteFinancierDTO {
    private Long idCompte;
    private Double solde;
    private String typeCompte;
    private Long utilisateurId;
}
