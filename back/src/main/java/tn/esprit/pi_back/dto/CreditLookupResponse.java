package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.StatutCredit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditLookupResponse {
    private Long id;
    private Double montantRestant;
    private StatutCredit statut;
}
